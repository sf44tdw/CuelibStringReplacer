/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuelibstringreplacer;

import FileSeeker.CueSheetFileSeeker;
import cuelibstringreplacer.Replacer.Replacer;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Util;

/**
 * CueSheetを探索し、<>～を問題の出にくい文字に置き換える。
 *
 * @author uname
 */
public class CuelibStringReplacer {

    /**
     * @param args 1:捜索対象のディレクトリ 2:サブディレクトリ探査の有無。1なら探査する。3:読み込みの際に前提とする文字コード
     *
     */
    public static void main(String[] args) {

        Logger log = Util.getCallerLogger();

        //finallyブロックからリソースを開放できるようにtryブロックの外で変数を宣言
        String charset = "JISAutoDetect";
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

                //文字コードの設定
                if (!(charset.length() == 0)) {
                    charset = args[2];
                    log.log(Level.INFO, "文字コード = {0}", charset);
                }

                List<Replacer> list_Replacer = Collections.synchronizedList(new ArrayList<Replacer>());

                Iterator<File> itf = res.iterator();
                while (itf.hasNext()) {
                    //対象ファイル
                    File target = itf.next();
                    log.log(Level.INFO, "置き換え対象ファイル = {0}", target);

                    Replacer rpr = new Replacer(target, charset);

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

        } catch (Exception e) {
            log.log(Level.SEVERE, "エラーです。", e);

        }
    }
}
