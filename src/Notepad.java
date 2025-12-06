import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * Java Swing で作るシンプルなメモ帳アプリ
 * 目的：GUIの基礎 + ファイル入出力の基礎を学ぶ
 */
public class Notepad extends JFrame {

    /** テキスト入力エリア（メモ帳の中心となる部分） */
    private JTextArea textArea;

    /** 今開いているファイル（上書き保存する時に使う。新規のときは null） */
    private File currentFile;




    /**
     * アプリ起動時に一度だけ呼ばれるコンストラクタ。
     * ウィンドウやメニューなどの初期設定を行う。
     */
    public Notepad() {
        super("My Notepad"); // ウィンドウ上部のタイトル

        // -------------------------------
        // ① テキストエリアを作る
        // -------------------------------
        textArea = new JTextArea();

        // テキストが画面外にはみ出したとき、自動で改行する設定
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // スクロール可能にするために JScrollPane で包む
        JScrollPane scrollPane = new JScrollPane(textArea);

        // ウィンドウにテキストエリアを配置（画面の中央領域）
        add(scrollPane, BorderLayout.CENTER);

        // -------------------------------
        // ② メニューバーを作成する
        // -------------------------------
        setJMenuBar(createMenuBar());

        // -------------------------------
        // ③ ウィンドウの基本設定
        // -------------------------------
        setSize(800, 600);                // 画面サイズ
        setDefaultCloseOperation(EXIT_ON_CLOSE); // × ボタンで終了
        setLocationRelativeTo(null);      // 画面中央に配置
    }

    /**
     * メニューバー全体を作成して返すメソッド。
     * 「ファイル」「編集」などのメニューがここで作られる。
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ---------------------------------
        // 「ファイル」メニューの作成
        // ---------------------------------
        JMenu fileMenu = new JMenu("ファイル");

        // --- 新規 ---
        JMenuItem newItem = new JMenuItem("新規");
        newItem.addActionListener(this::onNewFile);
        fileMenu.add(newItem);

        // --- 開く ---
        JMenuItem openItem = new JMenuItem("開く");
        openItem.addActionListener(this::onOpenFile);
        fileMenu.add(openItem);

        // --- 上書き保存 ---
        JMenuItem saveItem = new JMenuItem("上書き保存");
        saveItem.addActionListener(this::onSave);
        fileMenu.add(saveItem);

        // --- 名前を付けて保存 ---
        JMenuItem saveAsItem = new JMenuItem("名前を付けて保存");
        saveAsItem.addActionListener(this::onSaveAs);
        fileMenu.add(saveAsItem);

        fileMenu.addSeparator(); // 区切り線

        // --- 終了 ---
        JMenuItem exitItem = new JMenuItem("終了");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // ---------------------------------
        // 「編集」メニュー（例として「全削除」を追加）
        // ---------------------------------
        JMenu editMenu = new JMenu("編集");

        JMenuItem clearItem = new JMenuItem("全て削除");
        clearItem.addActionListener(e -> textArea.setText(""));
        editMenu.add(clearItem);

        menuBar.add(editMenu);

        return menuBar;
    }

    // ---------------------------------------------------------
    // ▼▼▼ メニューの各機能 ▼▼▼
    // ---------------------------------------------------------

    /**
     * 新規ファイルの動作：
     * ・テキストエリアを空にする
     * ・currentFile を null に戻す（新規だから）
     */
    private void onNewFile(ActionEvent e) {
        textArea.setText("");  // テキストを全部削除
        currentFile = null;    // まだ保存先はない
        setTitle("My Notepad - 新規ファイル"); // タイトル更新
    }

    /**
     * ファイルを開く動作
     * ・JFileChooser でファイル選択
     * ・選んだファイルを UTF-8 で読み込んで表示
     */
    private void onOpenFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();

        int result = chooser.showOpenDialog(this);

        // ファイルが選択された場合だけ処理を行う
        if (result == JFileChooser.APPROVE_OPTION) {

            currentFile = chooser.getSelectedFile(); // 選んだファイルを保持

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(currentFile), "UTF-8"))) {

                textArea.read(br, null); // テキストエリアに読み込み
                setTitle("My Notepad - " + currentFile.getName());

            } catch (IOException ex) {
                showError("ファイルを開けませんでした。");
            }
        }
    }

    /**
     * 上書き保存
     * ・currentFile が null → 新規なので「名前を付けて保存」に流す
     * ・そうでなければそのまま書き込む
     */
    private void onSave(ActionEvent e) {
        if (currentFile == null) {
            onSaveAs(e); // 保存先が決まってないので SAVE AS へ
        } else {
            writeToFile(currentFile); // 書き込みメソッドを使用
        }
    }

    /**
     * 名前を付けて保存
     * ・保存先ファイルを JFileChooser で選ばせる
     * ・writeToFile で保存
     */
    private void onSaveAs(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();

        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile(); // 新しい保存先
            writeToFile(currentFile);
            setTitle("My Notepad - " + currentFile.getName());
        }
    }

    /**
     * 実際にファイルへ書き込む処理。
     * ・UTF-8 で保存
     * ・textArea の内容をそのままファイルへ write
     */
    private void writeToFile(File file) {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {

            textArea.write(bw);

        } catch (IOException ex) {
            showError("ファイルを保存できませんでした。");
        }
    }

    /** エラーメッセージを表示する共通メソッド */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "エラー", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * アプリのエントリポイント（main メソッド）
     * Swing GUI は Event Dispatch Thread（EDT）で実行するのがルール。
     */
    public static void main(String[] args) {

        // OS の見た目に合わせる（Windows/macOS の見た目になる）
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // GUI はこのスレッドに任せるのが正しい書き方
        SwingUtilities.invokeLater(() -> {
            Notepad notepad = new Notepad();
            notepad.setVisible(true); // ウィンドウを表示
        });
    }
}
