/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author dosdiaopfhj
 */
public class Util {

    // クラス名をセットしたロガーを取得
    public final static synchronized Logger getCallerLogger() {
        return Logger.getLogger(new Throwable().getStackTrace()[1].getClassName());
    }

    //ロガーに設定ファイルをセットする。エラーの場合はデフォルトの設定を復元する。
    public final static synchronized void setlogproperties(String path) throws IOException {
        InputStream in;
        try {
            try {
                in = new FileInputStream(path);
                LogManager.getLogManager().readConfiguration(in);
                in.close();
                Util.getCallerLogger().log(Level.INFO, "ロガーの設定を行いました。設定ファイル={0}", path);
            } catch (FileNotFoundException ex) {
                LogManager.getLogManager().readConfiguration();
                Util.getCallerLogger().log(Level.WARNING,"ロガーの設定ファイルが見つかりません。",ex);
            }
        } catch (SecurityException e) {
           Util.getCallerLogger().log(Level.WARNING,"ロガーの設定時にセキュリティ例外が発生しました。",e);
        }
    }

}
