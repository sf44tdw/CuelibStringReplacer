/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuelibstringreplacer;

import cuelibstringreplacer.Replacer.Replacer;
import cuelibtools.attributeChecker.ProhibitedCharacterChecker;
import cuelibtools.CueSheetListMaker.CueSheetListMaker;
import cuelibtools.attributeChecker.AttributeChecker;
import cuelibtools.attributeChecker.MediaFileNameChecker;
import cuelibtools.attributeChecker.SynonymTrackTitleChacker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author uname
 */
public class CuelibStringReplacer {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    /**
     * @param args<br>
     * 1:捜索対象のディレクトリ<br>
     * 2:サブディレクトリ探査の有無。0なら探査しない。1なら探査する。設定に失敗した場合は0がセットされたものとみなす。<br>
     * 3:読み込みの際に前提とする文字コード<br>
     * 4:置き換えパターン設定ファイルの場所。<br>
     */
    public static void main(String[] args) {
        final org.slf4j.Logger log = LoggerFactory.getLogger("CuelibStringReplacer");
        //finallyブロックからリソースを開放できるようにtryブロックの外で変数を宣言
        BufferedReader rc = null;
        ICsvBeanReader inFile = null;

        try {

            //ファイルリストの作成
            log.info("探査ディレクトリ          " + args[0]);
            File Source = new File(args[0]);

            if (Source.isDirectory()) {

                log.info("検索対象を発見しました。");

                log.info("サブディレクトリ探査  " + args[1]);
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
                log.info("サブディレクトリ探査設定を完了。");

                Charset charset;
                try {
                    charset = Charset.forName(args[2]);
                } catch (IllegalArgumentException ex) {
                    log.warn("文字コードが設定できなかったため、システムの既定値で読み込みます。");
                    charset = Charset.defaultCharset();
                }
                log.info("読み込み文字コード={}", charset);

                List<AttributeChecker> checker = new ArrayList<>();
                checker.add(new MediaFileNameChecker());
                checker.add(new SynonymTrackTitleChacker());
                checker.add(new ProhibitedCharacterChecker());

                CueSheetListMaker fileListMaker = new CueSheetListMaker(Source, charset, checker, recursive);

                log.info("探査開始。");
                List<File> res = fileListMaker.MakeCueFileList();

                log.info("探査完了。件数= " + res.size());

                log.info("置き換え設定ファイル " + args[3]);
                File config = new File(args[3]);

                rc = new BufferedReader(new InputStreamReader(new FileInputStream(config), charset));
                inFile = new CsvBeanReader(rc, CsvPreference.EXCEL_PREFERENCE);
                final String[] header = inFile.getHeader(true);
                csvBean csvB = null;
                ConcurrentHashMap<String, String> confmap = new ConcurrentHashMap<>();
                while ((csvB = inFile.read(csvBean.class, header, csvBean.processors)) != null) {
                    confmap.put(csvB.getFrom(), csvB.getTo());
                }

                List<Replacer> list_Replacer = Collections.synchronizedList(new ArrayList<Replacer>());

                Iterator<File> itf = res.iterator();
                while (itf.hasNext()) {
                    //対象ファイル
                    File target = itf.next();

                    //出力先ファイルの作成
                    String outfile = target.getAbsolutePath() + "_REP_" + new Date().getTime() + ".cue";
                    File dest = new File(outfile);

                    Replacer rpr = new Replacer(target, dest, charset, confmap);

                    list_Replacer.add(rpr);
                }

                log.info("置き換え処理件数 " + list_Replacer.size());

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
            log.error("ファイルが見つかりません。", e);
        } catch (IOException e) {
            log.error("入出力エラーです。", e);
        } catch (Exception e) {
            log.error("エラーです。", e);
        } finally {
            //例外発生時にも確実にリソースが開放されるように
            //close()の呼び出しはfinallyブロックで行う。
            try {

                if (inFile != null) {
                    inFile.close();
                }

            } catch (Exception e) {
                log.error("エラーです。", e);
            }
        }
    }
}
