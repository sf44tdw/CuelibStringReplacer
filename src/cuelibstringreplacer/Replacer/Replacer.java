/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuelibstringreplacer.Replacer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jwbroek.util.StringReplacer;
import util.Util;

/**
 *
 * @author uname
 */
public class Replacer implements Runnable {

    private final File target;
    private final File dest;
    private BufferedReader r;
    private BufferedWriter w;
    private final String charset;
    private final StringReplacer rP;
    private static final Logger log = Util.getCallerLogger();

    public Replacer(File target, File dest, String charset, ConcurrentHashMap<String, String> confmap) {
        this.target = target;
        this.dest = dest;
        this.charset = charset;
        this.rP = new StringReplacer(confmap);
    }

    private synchronized boolean replace() {

        try {
            //ファイルの読み込み準備         
            this.r = new BufferedReader(new InputStreamReader(new FileInputStream(target), charset));
            //ファイルの書き込み準備
            this.w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest), charset));


            String in_line;
            String out_line;
            //一行ずつ読み込み→変換→書き込み。
            //readLine()がnullを返却した時点でEOFと判断し、ループを抜ける。
            while ((in_line = r.readLine()) != null) {
                out_line = rP.replace(in_line);

                w.write(out_line);
                w.newLine();

                //ログに記録
                log.log(Level.INFO, "{0} 出力ファイルへ書き込み {1} ->-> {2}", new Object[]{Thread.currentThread().getName(), in_line, out_line});
                
            }
            return true;
        } catch (UnsupportedEncodingException ex0) {
            log.log(Level.SEVERE,Thread.currentThread().getName()+" 文字コードの指定に問題があります。", ex0);
            return false;
        } catch (IOException ex1) {
            log.log(Level.SEVERE,Thread.currentThread().getName()+" 入出力エラーです。", ex1);
            return false;
        } catch (Exception ex2) {
            log.log(Level.SEVERE,Thread.currentThread().getName()+" その他のエラーです。", ex2);
            return false;
        } finally {
            //例外発生時にも確実にリソースが開放されるように
            //close()の呼び出しはfinallyブロックで行う。
            try {
                if (this.r != null) {
                    this.r.close();
                }
                if (this.w != null) {
                    this.w.close();
                }
            } catch (Exception ex3) {
                log.log(Level.SEVERE,Thread.currentThread().getName()+" その他のエラーです。", ex3);
                return false;
            }
        }
    }

    @Override
    public void run() {
        log.log(Level.INFO, "{0} 入力ファイル {1}", new Object[]{Thread.currentThread().getName(), this.target.toString()});
        log.log(Level.INFO, "{0} 出力ファイル  {1}", new Object[]{Thread.currentThread().getName(), this.dest.toString()});
        log.log(Level.INFO, "{0} 文字コード指定  {1}", new Object[]{Thread.currentThread().getName(), this.charset});
        log.log(Level.INFO, "{0} 置き換え開始", Thread.currentThread().getName());
        if (this.replace()) {
            log.log(Level.INFO, "{0} 置き換え成功", Thread.currentThread().getName());
        } else {
            log.log(Level.WARNING, "{0} 置き換え失敗", Thread.currentThread().getName());
        }
    }
}
