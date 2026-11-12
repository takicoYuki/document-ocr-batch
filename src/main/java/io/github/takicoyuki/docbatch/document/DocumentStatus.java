package io.github.takicoyuki.docbatch.document;

/**
 * 書類の処理ステータス。
 *
 * <pre>
 * RECEIVED ──(OCRバッチ)──┬─> COMPLETED     信頼度がしきい値以上
 *                         ├─> NEEDS_REVIEW  信頼度がしきい値未満（目視確認が必要）
 *                         └─> FAILED        外部API呼び出しがリトライ後も失敗
 * </pre>
 */
public enum DocumentStatus {
    RECEIVED,
    COMPLETED,
    NEEDS_REVIEW,
    FAILED
}
