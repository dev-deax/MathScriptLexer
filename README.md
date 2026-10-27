# MathScript Lexer V2

Proyecto Maven para NetBeans que implementa un analizador léxico manual del lenguaje simplificado **MathScript**.

## Requisitos

- JDK 17 o superior.
- NetBeans con soporte para proyectos Maven.
- No requiere dependencias externas.

## Abrir en NetBeans

1. Descomprime el ZIP.
2. En NetBeans selecciona **File > Open Project**.
3. Abre la carpeta `MathScriptLexer` (la que contiene `pom.xml`).
4. Ejecuta `mathscript.Main`.

## V2 — mejoras de interfaz y archivos

- Interfaz completamente **Dark Mode**, sin selector de tema.
- Paleta de resaltado ajustada específicamente al fondo oscuro para que reservadas, números, cadenas, operadores y delimitadores mantengan buena lectura.
- Botones `Abrir`, `Guardar`, `Limpiar` y `Analizar` con jerarquía visual, contraste, hover y estado presionado.
- Se eliminó el subtítulo que aparecía bajo `MathScript Lexer`.
- El editor se presenta dentro de una pestaña de documento.
- Documento nuevo: `Sin título`.
- Archivo abierto: se muestra su nombre en la pestaña y en la barra de título.
- Un asterisco `*` indica cambios todavía no guardados.
- `Guardar` crea un archivo nuevo la primera vez y luego guarda directamente sobre el archivo asociado.
- Al abrir un archivo, los cambios posteriores quedan asociados a ese mismo archivo.
- Antes de abrir otro archivo, limpiar o cerrar, se consulta qué hacer si hay cambios sin guardar.
- `Limpiar` crea un documento nuevo de forma segura y evita vaciar accidentalmente un archivo abierto.
- Tablas, consola de errores, números de línea, selección y scrolls usan la misma paleta oscura.

## Lexer

- Lexer manual, sin `Pattern`, `Matcher`, `String.matches()`, Flex ni Lex.
- Palabras reservadas: `var`, `print`, `if`, `else`.
- Identificadores, enteros, decimales, cadenas, operadores y delimitadores.
- Comentarios `//` como elementos ignorables.
- Errores con línea y columna.
- Tabla de símbolos con palabras reservadas e identificadores únicos.
- Editor `JTextPane` con números de línea dinámicos.
- Resaltado de sintaxis obtenido de los tokens producidos por el mismo Lexer.
- Actualización visual después de una pausa de 400 ms.
- Archivos de prueba en `ejemplos/`.

## Diferencia entre las tablas

- **Tokens:** muestra cada token visible reconocido, incluyendo operadores y delimitadores.
- **Tabla de símbolos:** mantiene únicamente palabras reservadas e identificadores únicos, con la primera línea donde aparecieron.

## Flujo principal

`Código -> Lexer -> ResultadoAnalisis -> Tokens / Símbolos / Errores -> GUI`

Mientras se escribe, el Lexer se usa únicamente para el resaltado visual. Las tablas y la consola se actualizan formalmente al presionar **Analizar**.

## Nota académica

La lógica léxica y el AFD se mantienen respecto de la versión anterior; la V2 se concentra en la experiencia del editor y el manejo de archivos. Antes de entregar o exponer, el grupo debe revisar y comprender cada decisión del AFD y del código.
