# 東華日曆

本專案是一款瀏覽國立東華大學行事曆的 Android 應用程式。

應用程式會依開始日期將行程分組，每組上方顯示日期與相對時間（例如「3 天後」），啟動時自動
捲動至今天之後的第一個行程。點選行程即可查看分類、類型、日期、天數、狀態與來源；國定假日與
寒暑假會以不同色彩標示，已結束的行程則會淡化。

向左或向右滑動行程可將其加入收藏；按下應用程式列左側的星號可只顯示收藏的行程，右側的篩選
按鈕則可依分類篩選。下拉可重新載入，載入失敗時也能重試並查看錯誤詳細資訊。

> 行事曆資料擷取自[國立東華大學行事曆](https://sys.ndhu.edu.tw/AA/calendar/)的公開頁面。
> 該網站並未提供官方 API，因此網站改版可能導致行事曆在未預警的情況下無法載入。本應用程式
> 並非東華大學官方軟體，僅供便利查閱；需要確認資料正確性時，請以學校公告為準。

## 安裝

請從[最新版本](https://github.com/000hen/ndhucal-android/releases/latest)下載適合裝置的 APK，開啟檔案後，依 Android 提示允許
瀏覽器或檔案管理員安裝應用程式。裝置需執行 Android 8.0 或更新版本。

- `ndhucalendar-arm64-v8a.apk` — 多數現行 Android 手機與平板
- `ndhucalendar-armeabi-v7a.apk` — 較舊的 32 位元 ARM 裝置
- `ndhucalendar-x86_64.apk` 或 `ndhucalendar-x86.apk` — Intel 裝置與模擬器

每個版本也會附上 `SHA256SUMS.txt`。請在下載檔案所在的目錄執行以下指令以驗證檔案：

```sh
sha256sum --check SHA256SUMS.txt
```

## 建置

你需要 Android SDK、Android NDK、JDK 21、Rust 與 `cargo-ndk`。請先初始化 Rust submodule，
再安裝四種 Android Rust targets：

```sh
git submodule update --init --recursive
cargo install cargo-ndk --locked
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

在專案根目錄建立 `local.properties`，指定 Android SDK 的位置：

```properties
sdk.dir=/path/to/android/sdk
```

建置時會自動以 `cargo ndk` 編譯 `rust/ndhucal-rs` 中的解析程式庫。你可以在 Android Studio
中開啟並執行專案，也可以使用命令列建置。在 POSIX shell 中使用 `./gradlew`，在 PowerShell
中則使用 `gradlew.bat`：

```sh
./gradlew assembleDebug   # 建置偵錯版 APK
./gradlew installDebug    # 建置並安裝至已連線的裝置
```

執行 `./gradlew assembleRelease -PreleasePerAbi=true` 會在 `app/build/outputs/apk/release/`
下產生四個未簽署、分別適用於不同架構的 APK；若省略該屬性，則產生單一 APK。若要簽署輸出
檔案，請提供下列四個環境變數，並加上 `--no-configuration-cache` 執行建置：

- `RELEASE_KEYSTORE_FILE`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

## 發布

發布 GitHub Release 時，`.github/workflows/release.yml` 會建置並簽署四個 APK、驗證簽章、
建立 `SHA256SUMS.txt`，並將這五個檔案附加至該版本。請先設定下列儲存庫密鑰：

- `RELEASE_KEYSTORE_BASE64` — 以 Base64 編碼的完整金鑰庫
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

在 PowerShell 中可用以下指令建立 Base64 值：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))
```

每個版本都必須使用相同的簽署憑證；若更新版 APK 使用不同憑證簽署，Android 將拒絕把它安裝為
既有應用程式的更新。

## 授權條款

本專案採用 [MIT License](LICENSE) 授權。
