#### Реализация домашнего задания ["Основные компоненты Android-приложения"](https://gitlab.com/Mishabuzov/tfs-android-online-autumn-2020/-/tree/master/HomeWork_1)
* На старте открывается [__ContactsActivity__](https://gitlab.com/Mishabuzov/tfs-android-online-autumn-2020/-/blob/homework_1/HomeWork_1/LocalBroadcastReceiverHW/app/src/main/java/ru/home/localbroadcastreceiverhw/contacts_screen/ContactsActivity.kt), из неё открывается [__ServiceActivity__](https://gitlab.com/Mishabuzov/tfs-android-online-autumn-2020/-/blob/homework_1/HomeWork_1/LocalBroadcastReceiverHW/app/src/main/java/ru/home/localbroadcastreceiverhw/service/ServiceActivity.kt) с ожиданием результата.
* Из [__ServiceActivity__](https://gitlab.com/Mishabuzov/tfs-android-online-autumn-2020/-/blob/homework_1/HomeWork_1/LocalBroadcastReceiverHW/app/src/main/java/ru/home/localbroadcastreceiverhw/service/ServiceActivity.kt) запускается [__ExtractContactsService__](https://gitlab.com/Mishabuzov/tfs-android-online-autumn-2020/-/blob/homework_1/HomeWork_1/LocalBroadcastReceiverHW/app/src/main/java/ru/home/localbroadcastreceiverhw/service/ExtractContactsService.kt) извлекающий [информацию о контактах](https://gitlab.com/Mishabuzov/tfs-android-online-autumn-2020/-/blob/homework_1/HomeWork_1/LocalBroadcastReceiverHW/app/src/main/java/ru/home/localbroadcastreceiverhw/Contact.kt) через __ContentProvider__, и далее передающий её обратно в [__ServiceActivity__](https://gitlab.com/Mishabuzov/tfs-android-online-autumn-2020/-/blob/homework_1/HomeWork_1/LocalBroadcastReceiverHW/app/src/main/java/ru/home/localbroadcastreceiverhw/service/ServiceActivity.kt) через __LocalBroadcastReceiver__.
* [__ServiceActivity__](https://gitlab.com/Mishabuzov/tfs-android-online-autumn-2020/-/blob/homework_1/HomeWork_1/LocalBroadcastReceiverHW/app/src/main/java/ru/home/localbroadcastreceiverhw/service/ServiceActivity.kt) пробрасывает полученный результат в [__ContactsActivity__](https://gitlab.com/Mishabuzov/tfs-android-online-autumn-2020/-/blob/homework_1/HomeWork_1/LocalBroadcastReceiverHW/app/src/main/java/ru/home/localbroadcastreceiverhw/contacts_screen/ContactsActivity.kt), которая в свою очередь отображает полученные контакты (if any).

Структура проекта:
```
$project_path
    ├── contacts_screen
            ├── ContactsActivity.kt     - 1ая activity отображающая рез-т
            └── ContactsAdapter.kt
            
    ├── service
            ├── ExtractContactsService.kt    - сервис для извлечения контактов
            └── ServiceActivity.kt                   - activity, запускающая сервис
            
    ├── widgets     - вспомогательные классы для отображения
            ├── DividerItemDecoration.kt
            └── EmptyRecyclerView.kt
            
    └── Contact.kt  - кастомная модель контактов
```