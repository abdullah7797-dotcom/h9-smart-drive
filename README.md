# H9 Smart Drive

## التنزيل عبر Downloader

استخدم رابط APK المباشر التالي داخل تطبيق Downloader:

https://github.com/abdullah7797-dotcom/h9-smart-drive/releases/download/v1.3.0-beta.1/H9-Smart-Drive.apk

SHA-256:

`d012dbaf338f4caf753c650b94b7e4a60f6a742aee557db2b6133077949840f5`

نسخة MVP أصلية لتطبيق شاشة Haval H9، مكتوبة بـKotlin وJetpack Compose، وتعمل من Android 9 (API 28) فما فوق. المشروع مصمم للعمل دون Root أو تعديل Firmware، ويفصل البيانات التجريبية بوضوح عن البيانات الحقيقية.

## ما يعمل في النسخة الحالية

- لوحة قيادة أفقية حديثة مع العربية RTL والإنجليزية.
- الوضعان النهاري والليلي.
- عداد السرعة وRPM والوقود والمدى وحرارة المحرك والبطارية.
- بطاقة ضغط الإطارات وحالة المركبة.
- وضع رحلات برية يتضمن البوصلة والموقع والارتفاع ونقاط الرحلة.
- اكتشاف GPS وAndroid Automotive وحزمة GWM المعلنة وتطبيقات الخرائط والوسائط.
- سرعة وموقع وارتفاع واتجاه حقيقي عبر GPS بعد منح الصلاحية.
- تشغيل تطبيق الخرائط أو الوسائط الأصلي عبر Android Intents.
- وضع Demo محلي واضح ومستقل.
- تخزين Room للرحلات والصيانة، وDataStore للإعدادات.
- صفحة تشخيص لا تعرض VIN أو الإحداثيات أو المعرفات الشخصية في تقريرها.

## حدود هذه النسخة

موفّر GWM الحالي يكتشف الحزمة فقط ولا يرتبط بعقد Binder مجهول. يلزم تشغيل التطبيق على شاشة السيارة، جمع تقرير التشخيص، ثم توثيق AIDL/الخدمة والصلاحيات المتاحة قبل إضافة القراءة الفعلية. لا تحتوي النسخة على أي كتابة إلى السيارة أو CAN Bus.

## البناء

المتطلبات:

- JDK 17.
- Android SDK Platform 35.
- Android SDK Build Tools 34 أو أحدث.

```bash
./gradlew assembleDebug
```

النتيجة:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## الاختبار

```bash
./gradlew testDebugUnitTest lintDebug
```

## التثبيت

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -W -n com.h9smartdrive.app.debug/com.h9smartdrive.app.MainActivity
```

راجع [دليل اختبار السيارة](docs/VEHICLE_TESTING_AR.md) قبل الاستخدام على الشاشة الأصلية.

## السلامة

هذا التطبيق أداة معلومات غير معتمدة ولا يستبدل عدادات السيارة الأصلية أو التنبيهات أو الملاحة أو معدات السلامة. لا تستخدم الإعدادات والتخصيص أثناء القيادة.
