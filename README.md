# Разхуяриватель и Захуяриватель

Комплексная утилита неуправляемой деструкции файлов и последующего их полного восстановления.

`java ru.alljoint.crashutils.CrashFiles <path>` - разрушает до 64 (в зависимости от размера файла) случайных байт на один файл.
Делается это в два прохода. Первый проход проверяет все ли файлы могут быть открыты в данной папке. Второй проход выполняет
саму неуправляемую деструкцию записывая в каждый файл до 64 случайных байт по случайным позициям, при этом записывая оригинальные
значения данных по этим позициям в файл restoreinfo.data в текстовом виде. <path> - путь к папке для файлов которой должна быть
выполнена неуправляемая деструкция. Неуправляемая деструкция выполняется для ВСЕХ файлов во всем дереве папок по указанному пути.

`java ru.alljoint.crashutils.RestoreAfterCrash <restoreinfo.data>` - полностью восстанавливает файлы разрушенные утилитой
`java ru.alljoint.crashutils.CrashFiles` информация о разрушении которых содержится в файле `<restoreinfo.data>`.