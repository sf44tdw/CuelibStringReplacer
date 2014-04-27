/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuelibstringreplacer.Replacer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Util;

/**
 *
 * @author uname
 */
public class Replacer implements Runnable {

    private static final Logger log = Util.getCallerLogger();
    
    //メンバ変数はスレッドセーフではない可能性があるので、マルチスレッドで動かすメソッド全てにロックを掛けることとした。
    private String charset;
    private File File_input;

    public Replacer(File File_input, String charset) {
        this.charset = charset;
        this.File_input = File_input;
    }

    private synchronized BufferedReader open_Istream() {
        try {
            //読み込み用ストリームの作成
            log.log(Level.FINEST, Thread.currentThread().getName() + " 読み込み用ストリームを作成します。 読み込み用ファイル名 = {0}", File_input.getAbsolutePath());
            BufferedReader inFilereader = new BufferedReader(new InputStreamReader(new FileInputStream(File_input), charset));
            log.log(Level.FINEST, "{0} 読み込み用ストリームを作成。", Thread.currentThread().getName());
            return inFilereader;

        } catch (UnsupportedEncodingException ex) {
            log.log(Level.SEVERE, Thread.currentThread().getName() + " 文字コードの設定に問題があります。文字コード=" + charset, ex);
            return null;
        } catch (FileNotFoundException ex) {
            log.log(Level.SEVERE, Thread.currentThread().getName() + " ファイルが見つかりません。", ex);
            return null;
        }
    }

    private synchronized BufferedWriter open_Ostream() {
        try {
            //出力先ファイルの作成
            SimpleDateFormat DF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String outfile = File_input.getAbsolutePath() + "_REP_" + DF.format(new Date()) + ".cue";
            File File_output = new File(outfile);
            log.log(Level.FINEST, Thread.currentThread().getName() + " 書き込み用ストリームを作成します。 書き込み用ファイル名 = {0}", File_output.getAbsolutePath());
            BufferedWriter outFilewriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(File_output), charset));
            log.log(Level.FINEST, "{0} 書き込み用ストリームを作成。", Thread.currentThread().getName());
            return outFilewriter;

        } catch (UnsupportedEncodingException ex) {
            log.log(Level.SEVERE, Thread.currentThread().getName() + " 文字コードの設定に問題があります。文字コード=" + charset, ex);
            return null;
        } catch (FileNotFoundException ex) {
            log.log(Level.SEVERE, Thread.currentThread().getName() + " ファイルが見つかりません。", ex);
            return null;
        }
    }

    private synchronized void close_Istreams(BufferedReader inFilereader) {
        try {
            inFilereader.close();
            log.log(Level.FINEST, "{0} 読み込み用ストリームを閉鎖。", Thread.currentThread().getName());
        } catch (IOException ex) {
            log.log(Level.SEVERE, Thread.currentThread().getName() + " 読み込み用ストリームの閉鎖に失敗しました。", ex);
        }
    }

    private synchronized void close_Ostreams(BufferedWriter outFilewriter) {
        try {
            outFilewriter.flush();
            outFilewriter.close();
            log.log(Level.FINEST, "{0} 書き込み用ストリームを閉鎖。", Thread.currentThread().getName());
        } catch (IOException ex) {
            log.log(Level.SEVERE, Thread.currentThread().getName() + " 書き込み用ストリームの閉鎖に失敗しました。", ex);
        }
    }

    private synchronized List<String> read_Inputfile() {
        BufferedReader in_r = this.open_Istream();

        if (this.open_Istream() != null) {
            try {
                List<String> inputBuffer = Collections.synchronizedList(new ArrayList<String>());
                String str;
                while ((str = in_r.readLine()) != null) {
                    inputBuffer.add(str);
                    log.log(Level.FINEST, "{0} バッファに1行追加しました。 行 = {1}", new Object[]{Thread.currentThread().getName(), str});
                }
                log.log(Level.FINEST, "{0} バッファへの読み込みを完了しました。行数 = {1}", new Object[]{Thread.currentThread().getName(), inputBuffer.size()});
                this.close_Istreams(in_r);
                return inputBuffer;
            } catch (IOException ex) {
                log.log(Level.SEVERE, Thread.currentThread().getName() + " ファイルからの読み込みに失敗しました。ファイル = " + this.File_input.getAbsolutePath(), ex);
                this.close_Istreams(in_r);
                return null;
            }
        } else {
            return null;
        }
    }

    private synchronized List<String> replace_Backend() {
        List<String> outputBuffer = Collections.synchronizedList(new ArrayList<String>());
        boolean flag_Changed = false;
        for (String sb : read_Inputfile()) {
            String sa;
            sa = sb.replaceAll("<", "(");
            sa = sa.replaceAll(">", ")");
            sa = sa.replaceAll("～", "-");
            sa = sa.replaceAll("〜", "-");

            outputBuffer.add(sa);
            if (sa.equals(sb)) {
                log.log(Level.INFO, "{0} 置き換えられた文字列が存在しない行です。置き換え前 = {1} 置き換え後={2}", new Object[]{Thread.currentThread().getName(), sb, sa});
            } else {
                flag_Changed = true;
                log.log(Level.INFO, "{0} 置き換えられた文字列が存在する行です。置き換え前 = {1} 置き換え後={2}", new Object[]{Thread.currentThread().getName(), sb, sa});
            }
        }
        if (flag_Changed == true) {
            log.log(Level.INFO, "{0} 置き換え有り。", Thread.currentThread().getName());
            return outputBuffer;
        } else {
            log.log(Level.INFO, "{0} 置き換えなし。", Thread.currentThread().getName());
            return null;
        }
    }

    private synchronized boolean replace() {
        BufferedWriter ou_w = this.open_Ostream();

        List<String> L = this.replace_Backend();
        if (L != null) {
            if (ou_w != null) {
                try {
                    for (Iterator<String> i = L.iterator(); i.hasNext();) {
                        ou_w.write(i.next());
                        ou_w.newLine();
                    }
                    ou_w.flush();
                    this.close_Ostreams(ou_w);
                } catch (IOException ex) {
                    log.log(Level.SEVERE, Thread.currentThread().getName() + " 書き込みに失敗しました。", ex);
                    return false;
                }
            } else {
                log.log(Level.WARNING, "{0} 出力の準備が出来ませんでした。", Thread.currentThread().getName());
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "{0} 入力ファイル {1}", new Object[]{Thread.currentThread().getName(), this.File_input.toString()});
        log.log(Level.INFO, "{0} 文字コード指定  {1}", new Object[]{Thread.currentThread().getName(), this.charset});
        log.log(Level.INFO, "{0} 置き換え開始", Thread.currentThread().getName());
        if (this.replace()) {
            log.log(Level.INFO, "{0} 置き換え成功", Thread.currentThread().getName());
        } else {
            log.log(Level.WARNING, "{0} 置き換え失敗", Thread.currentThread().getName());
        }
    }

    @Override
    public final void finalize() {
        // 何もしない(OBJ11-J. コンストラクタが例外をスローする場合には細心の注意を払う)
    }
}
