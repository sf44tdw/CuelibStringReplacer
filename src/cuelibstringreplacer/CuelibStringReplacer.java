/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuelibstringreplacer;

import cuelibstringreplacer.Replacer.Replacer;
import cuelibtools.FileSeeker.CueSheetFileSeeker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import util.Util;

/**
 *
 * @author uname
 */
public class CuelibStringReplacer {

    /**
     * @param args 1:捜索対象のディレクトリ 2:サブディレクトリ探査の有無。1なら探査する。 3:読み込みの際に前提とする文字コード
     * 4:置き換えパターン設定ファイルの場所。
     */
    public static void main(String[] args) {

        Logger log = Util.getCallerLogger();

        //finallyブロックからリソースを開放できるようにtryブロックの外で変数を宣言
        BufferedReader rc = null;
        ICsvBeanReader inFile = null;

        try {

            //ファイルリストの作成
            log.log(Level.INFO, "探査ディレクトリ={0}", args[0]);
            File Source = new File(args[0]);

            if (Source.isDirectory()) {
                CueSheetFileSeeker seeker = new CueSheetFileSeeker(Source);
                log.info("検索対象を発見しました。");

                log.log(Level.INFO, "サブディレクトリ探査={0}", args[1]);
                boolean recursive = false;
                switch (args[1]) {
                    case "0":
                        recursive = false;
                        break;

                    case "1":
                        recursive = true;
                        break;
                }

                if (recursive == false) {
                    log.info("サブディレクトリ探査は行いません。");
                }

                if (recursive == true) {
                    log.info("サブディレクトリ探査を行います。");
                }
                seeker.setRecursive(recursive);
                log.info("サブディレクトリ探査設定を完了。");

                log.info("探査開始。");
                List<File> res = seeker.seek();
                log.log(Level.INFO, "探査完了。件数 = {0}", res.size());

                log.log(Level.INFO, "置き換え設定ファイル = {0}", args[3]);
                File config = new File(args[3]);
                rc = new BufferedReader(new InputStreamReader(new FileInputStream(config), args[2]));
                inFile = new CsvBeanReader(rc, CsvPreference.EXCEL_PREFERENCE);
                final String[] header = inFile.getHeader(true);
                csvBean csvB;
                ConcurrentHashMap<String, String> confmap = new ConcurrentHashMap<>();
                while ((csvB = inFile.read(csvBean.class, header, csvBean.processors)) != null) {
                    confmap.put(csvB.getFrom(), csvB.getTo());
                }

                List<Replacer> list_Replacer = Collections.synchronizedList(new ArrayList<Replacer>());

                Iterator<File> itf = res.iterator();
                while (itf.hasNext()) {
                    //対象ファイル
                    File target = itf.next();
                    log.log(Level.INFO, "置き換え対象ファイル = {0}", target);

                    //出力先ファイルの作成
                    SimpleDateFormat DF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                    String outfile = target.getAbsolutePath() + "_REP_" + DF.format(new Date() + ".cue");
                    File dest = new File(outfile);

                    Replacer rpr = new Replacer(target, dest, args[2], confmap);

                    list_Replacer.add(rpr);
                }

                log.log(Level.INFO, "置き換え処理件数 = {0}", list_Replacer.size());

                List<Thread> list_Thread = Collections.synchronizedList(new ArrayList<Thread>());

                Iterator<Replacer> itr = list_Replacer.iterator();
                while (itr.hasNext()) {
                    Replacer rpre = itr.next();
                    Thread th = new Thread(rpre);
                    list_Thread.add(th);
                    th.start();
                }

            }

        } catch (FileNotFoundException e) {
            log.log(Level.SEVERE, "ファイルが見つかりません。", e);
        } catch (UnsupportedEncodingException e) {
            log.log(Level.SEVERE, "文字コードの指定に問題があります。", e);
        } catch (IOException e) {
            log.log(Level.SEVERE, "入出力エラーです。", e);
        } catch (Exception e) {
            log.log(Level.SEVERE, "エラーです。", e);
        } finally {
            //例外発生時にも確実にリソースが開放されるように
            //close()の呼び出しはfinallyブロックで行う。
            try {

                if (inFile != null) {
                    inFile.close();
                }

            } catch (IOException e) {
                log.log(Level.SEVERE, "エラーです。", e);
            }
        }
    }
}
