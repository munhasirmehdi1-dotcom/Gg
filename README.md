# Jacquard Studio (जैकवार्ड स्टूडियो CAD)

Convert any sketch or image into high-precision Jacquard textile design Windows BMP files with custom dimensions, color quantization, weave patterns, and WhatsApp sharing.

---

## 🔗 Live Application Links

- **Shared App Link (लाइव ऐप लिंक):**  
  [https://ais-pre-adg5q2qdu3jn7knazn2npc-645499485993.asia-east1.run.app](https://ais-pre-adg5q2qdu3jn7knazn2npc-645499485993.asia-east1.run.app)

- **Development App Link (डेवलपमेंट लिंक):**  
  [https://ais-dev-adg5q2qdu3jn7knazn2npc-645499485993.asia-east1.run.app](https://ais-dev-adg5q2qdu3jn7knazn2npc-645499485993.asia-east1.run.app)

---

## 📱 How to Download APK / Source Code

1. **Direct APK Download:**
   - In the Google AI Studio top-right navigation bar, click the **Settings (⚙️) / Export** icon.
   - Select **"Download APK"** or **"Generate APK"** to get the installable `.apk` file for your Android phone.

2. **Source Code (ZIP):**
   - Click **"Export as ZIP"** from the top menu to download the complete source code.

3. **Push to GitHub:**
   - You can link and push this repository directly to GitHub using the **GitHub** button in the top bar.

---

## 🌟 Key Features

1. **Sketch to Jacquard BMP File Generator:**
   - Generates Windows 8-Bit Indexed BMP (with 256-color palette table) and 24-Bit RGB BMP files compatible with Bonas, Staubli, Muller, and all textile CAD software.

2. **Direct Size Typing (चौड़ाई व ऊंचाई टाइप करें):**
   - Type exact Warp Hooks (Ends) and Weft Picks (Lines) directly into numeric fields.
   - Aspect ratio lock toggle to maintain original sketch proportion or create arbitrary custom dimensions.
   - Fabric Centimeter (cm) mode and Wallpaper resolution typing mode.

3. **Interactive Zoom Viewer (ज़ूम करके देखें):**
   - Inspect sketches before converting with pinch-to-zoom (up to 2000%), pan, zoom buttons (+/-), and full-screen view.

4. **Multi-Source Sketch Upload:**
   - Upload BMP, PNG, JPG, JPEG, and WEBP files.
   - Take camera photos of hand-drawn paper sketches.
   - In-app sketchpad to draw designs directly on screen.
   - Pre-loaded sample textile motif library (Paisley/Kalka, Damask, Diamond Weave, Saree Border).

5. **Color Quantization & Palette Editor:**
   - Quantize sketches into 2 to 16 distinct loom colors.
   - Stray pixel filter (despeckle) to eliminate isolated dots and prevent thread breakage.
   - Custom palette editor with RGB sliders, HEX codes, and traditional yarn swatches (Gold Zari, Silver Zari, Maroon, Emerald, Royal Blue, etc.).

6. **Weave Simulation & 8×8 Point Paper Grid:**
   - 8×8 Point Paper technical textile CAD grid with coordinate inspection (Hook #, Pick #, Yarn Color).
   - Real fabric weave simulation (Plain 1/1, Twill 2/2, Twill 3/1, Satin 5, Satin 8, Basket weave).

7. **Direct WhatsApp Sharing:**
   - Share Windows BMP file directly to WhatsApp with full technical specification sheet.
   - Share high-resolution PNG preview picture directly to WhatsApp chat.
   - Save BMP file to device Downloads.
   - English and Hindi bilingual interface toggle.

---

## 🛠️ Build Commands

To build the APK locally:

```bash
gradle :app:assembleDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

To run unit tests:

```bash
gradle :app:testDebugUnitTest
```
