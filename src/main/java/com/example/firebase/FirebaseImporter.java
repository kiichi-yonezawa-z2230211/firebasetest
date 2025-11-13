package com.example.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * FirebaseにJSONデータをインポートするクラス
 */
public class FirebaseImporter {

    private static Firestore db;

    public static void main(String[] args) {
        try {
            // Firebase初期化
            initializeFirebase("path/to/serviceAccountKey.json");

            // JSONファイルからデータをインポート
            String jsonFilePath = "data/import-data.json";
            String collectionName = "users"; // インポート先のコレクション名
            
            importJsonToFirestore(jsonFilePath, collectionName);

            System.out.println("データのインポートが完了しました！");

        } catch (Exception e) {
            System.err.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Firebaseを初期化する
     * 
     * @param serviceAccountKeyPath サービスアカウントキーのJSONファイルパス
     * @throws Exception 初期化エラー
     */
    private static void initializeFirebase(String serviceAccountKeyPath) throws Exception {
        FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyPath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        
        db = FirestoreClient.getFirestore();
        
        System.out.println("Firebaseの初期化が完了しました");
    }

    /**
     * JSONファイルからFirestoreにデータをインポートする
     * 
     * @param jsonFilePath インポートするJSONファイルのパス
     * @param collectionName インポート先のコレクション名
     * @throws Exception インポートエラー
     */
    private static void importJsonToFirestore(String jsonFilePath, String collectionName) 
            throws Exception {
        
        Gson gson = new Gson();
        FileReader reader = new FileReader(jsonFilePath);

        // JSONをMapのリストとして読み込む
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> data = gson.fromJson(reader, type);
        reader.close();

        // Firestoreにデータを書き込む
        int count = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String documentId = entry.getKey();
            Object documentData = entry.getValue();

            // ドキュメントをFirestoreに保存
            db.collection(collectionName)
                .document(documentId)
                .set(documentData, SetOptions.merge())
                .get(); // 同期的に実行

            count++;
            System.out.println("インポート済み: " + documentId);
        }

        System.out.println("合計 " + count + " 件のドキュメントをインポートしました");
    }

    /**
     * 配列形式のJSONをインポートする場合（ドキュメントIDを自動生成）
     * 
     * @param jsonFilePath インポートするJSONファイルのパス
     * @param collectionName インポート先のコレクション名
     * @throws Exception インポートエラー
     */
    @SuppressWarnings("unchecked")
    private static void importJsonArrayToFirestore(String jsonFilePath, String collectionName) 
            throws Exception {
        
        Gson gson = new Gson();
        FileReader reader = new FileReader(jsonFilePath);

        // JSONを配列として読み込む
        Type type = new TypeToken<java.util.List<Map<String, Object>>>(){}.getType();
        java.util.List<Map<String, Object>> dataList = gson.fromJson(reader, type);
        reader.close();

        // Firestoreにデータを書き込む
        int count = 0;
        for (Map<String, Object> data : dataList) {
            // ドキュメントIDを自動生成してFirestoreに保存
            db.collection(collectionName)
                .document() // IDを自動生成
                .set(data)
                .get(); // 同期的に実行

            count++;
        }

        System.out.println("合計 " + count + " 件のドキュメントをインポートしました");
    }
}
