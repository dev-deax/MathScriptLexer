from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A3, landscape
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph, Table, TableStyle


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "output" / "pdf" / "AFD_MathScriptLexer_Corregido.pdf"
PREVIEW = ROOT / "target" / "afd-graph-preview.png"
PAGE = landscape(A3)
WIDTH, HEIGHT = PAGE


def register_fonts():
    regular = Path("C:/Windows/Fonts/arial.ttf")
    bold = Path("C:/Windows/Fonts/arialbd.ttf")
    if regular.exists() and bold.exists():
        pdfmetrics.registerFont(TTFont("DocRegular", str(regular)))
        pdfmetrics.registerFont(TTFont("DocBold", str(bold)))
        return "DocRegular", "DocBold"
    return "Helvetica", "Helvetica-Bold"


FONT, FONT_BOLD = register_fonts()
NAVY = colors.HexColor("#17324D")
BLUE = colors.HexColor("#2F6FED")
PALE_BLUE = colors.HexColor("#EAF1FF")
GREEN = colors.HexColor("#198754")
PALE_GREEN = colors.HexColor("#EAF7EF")
RED = colors.HexColor("#C93C47")
PALE_RED = colors.HexColor("#FDEDEF")
TEXT = colors.HexColor("#243447")
MUTED = colors.HexColor("#617286")
GRID = colors.HexColor("#C9D4E0")
ROW_ALT = colors.HexColor("#F5F8FB")


BODY = ParagraphStyle(
    "body", fontName=FONT, fontSize=8.3, leading=10.3,
    textColor=TEXT, alignment=TA_LEFT,
)
BODY_SMALL = ParagraphStyle(
    "body-small", parent=BODY, fontSize=7.8, leading=9.5,
)
HEADER = ParagraphStyle(
    "header", parent=BODY, fontName=FONT_BOLD, fontSize=8.4,
    leading=10, textColor=colors.white,
)


def p(text, style=BODY):
    return Paragraph(text, style)


def page_header(c, title, subtitle, number):
    c.setFillColor(NAVY)
    c.rect(0, HEIGHT - 67, WIDTH, 67, fill=1, stroke=0)
    c.setFillColor(colors.white)
    c.setFont(FONT_BOLD, 21)
    c.drawString(36, HEIGHT - 31, title)
    c.setFont(FONT, 10.5)
    c.setFillColor(colors.HexColor("#D8E5F2"))
    c.drawString(36, HEIGHT - 50, subtitle)
    c.setFont(FONT_BOLD, 9)
    c.drawRightString(WIDTH - 36, HEIGHT - 36, f"{number} / 4")


def page_footer(c):
    c.setStrokeColor(GRID)
    c.line(36, 27, WIDTH - 36, 27)
    c.setFillColor(MUTED)
    c.setFont(FONT, 7.5)
    c.drawString(36, 15, "MathScriptLexer - AFD formal y controlador léxico")
    c.drawRightString(WIDTH - 36, 15,
                      "Las flechas consumen un carácter; CR y LF se consideran por separado.")


def draw_graph_page(c):
    page_header(
        c,
        "AFD corregido de MathScriptLexer",
        "27 estados A-AA, recorrido formal y separación explícita del fin de lexema",
        1,
    )
    if not PREVIEW.exists():
        raise FileNotFoundError(f"No existe la vista previa: {PREVIEW}")
    image = ImageReader(str(PREVIEW))
    iw, ih = image.getSize()
    max_w = WIDTH - 72
    max_h = HEIGHT - 112
    scale = min(max_w / iw, max_h / ih)
    draw_w = iw * scale
    draw_h = ih * scale
    x = (WIDTH - draw_w) / 2
    y = 36 + (max_h - draw_h) / 2
    c.drawImage(image, x, y, draw_w, draw_h, preserveAspectRatio=True, mask="auto")
    page_footer(c)
    c.showPage()


STATES_1 = [
    ("A", "INICIO", "No", "blanco/tab/CR/LF -> B; comilla -> C; delimitador -> D; + - * = -> F; / -> G; digito -> H; e/i/p/v -> J/K/L/M; otra letra o _ -> I", "Punto u otro caracter invalido -> E"),
    ("B", "ESPACIO", "WHITESPACE", "blanco, tab, CR o LF -> B", "Emitir bloque ignorable; volver a A sin consumir el siguiente caracter"),
    ("C", "CADENA ABIERTA", "No", "comilla -> N; cualquier otro salvo CR/LF -> C", "CR, LF o fin de entrada -> E"),
    ("D", "DELIMITADOR", "DELIM", "El delimitador ya fue consumido", "Emitir DELIM; volver a A sin consumir el siguiente caracter"),
    ("E", "ERROR / SUMIDERO", "No", "Durante la unidad defectuosa, cualquier caracter recuperable -> E", "Reportar un error y reiniciar en A desde el siguiente punto estable"),
    ("F", "OPERADOR SIMPLE", "OP", "+, -, * o = ya consumido", "Emitir OP; volver a A sin consumir el siguiente caracter"),
    ("G", "BARRA", "OP", "/ -> O", "Si no aparece otra /, emitir OP para la primera barra y reprocesar lo pendiente"),
    ("H", "ENTERO", "INT", "digito -> H; punto -> P", "Emitir INT; volver a A sin consumir el siguiente caracter"),
    ("I", "IDENTIFICADOR", "ID", "letra, digito o _ -> I", "Emitir ID; volver a A sin consumir el siguiente caracter"),
    ("J", "PREFIJO e", "ID", "l -> Q; otra letra, digito o _ -> I", "Emitir ID si el lexema termina en e"),
    ("K", "PREFIJO i", "ID", "f -> R; otra letra, digito o _ -> I", "Emitir ID si el lexema termina en i"),
    ("L", "PREFIJO p", "ID", "r -> S; otra letra, digito o _ -> I", "Emitir ID si el lexema termina en p"),
    ("M", "PREFIJO v", "ID", "a -> T; otra letra, digito o _ -> I", "Emitir ID si el lexema termina en v"),
    ("N", "CADENA CERRADA", "STRING", "La comilla de cierre ya fue consumida", "Emitir STRING; volver a A sin consumir el siguiente caracter"),
]


STATES_2 = [
    ("O", "COMENTARIO //", "COMMENT", "cualquier caracter salvo CR/LF -> O", "CR, LF o fin: emitir comentario ignorable; el salto queda pendiente"),
    ("P", "PUNTO TRAS ENTERO", "No", "digito -> U", "Otro caracter o fin de entrada -> E: decimal incompleto"),
    ("Q", "PREFIJO el", "ID", "s -> V; otra letra, digito o _ -> I", "Emitir ID si el lexema termina en el"),
    ("R", "PALABRA if", "ID / IF", "letra, digito o _ -> I", "Si termina aqui, la prioridad selecciona IF; si continua, es ID"),
    ("S", "PREFIJO pr", "ID", "i -> W; otra letra, digito o _ -> I", "Emitir ID si el lexema termina en pr"),
    ("T", "PREFIJO va", "ID", "r -> X; otra letra, digito o _ -> I", "Emitir ID si el lexema termina en va"),
    ("U", "DECIMAL", "FLOAT", "digito -> U; segundo punto -> E", "Emitir FLOAT; volver a A sin consumir el siguiente caracter"),
    ("V", "PREFIJO els", "ID", "e -> Y; otra letra, digito o _ -> I", "Emitir ID si el lexema termina en els"),
    ("W", "PREFIJO pri", "ID", "n -> Z; otra letra, digito o _ -> I", "Emitir ID si el lexema termina en pri"),
    ("X", "PALABRA var", "ID / VAR", "letra, digito o _ -> I", "Si termina aqui, la prioridad selecciona VAR; si continua, es ID"),
    ("Y", "PALABRA else", "ID / ELSE", "letra, digito o _ -> I", "Si termina aqui, la prioridad selecciona ELSE; si continua, es ID"),
    ("Z", "PREFIJO prin", "ID", "t -> AA; otra letra, digito o _ -> I", "Emitir ID si el lexema termina en prin"),
    ("AA", "PALABRA print", "ID / PRINT", "letra, digito o _ -> I", "Si termina aqui, la prioridad selecciona PRINT; si continua, es ID"),
]


def draw_state_table(c, rows, title, subtitle, page_no):
    page_header(c, title, subtitle, page_no)
    data = [[p("Estado", HEADER), p("Significado", HEADER), p("Aceptación", HEADER),
             p("Transiciones que consumen entrada", HEADER),
             p("Cuando el lexema ya termino", HEADER)]]
    for code, name, accept, transitions, boundary in rows:
        data.append([
            p(f"<b>{code}</b>"), p(name), p(accept),
            p(transitions, BODY_SMALL), p(boundary, BODY_SMALL),
        ])
    table = Table(data, colWidths=[48, 145, 105, 420, 400], repeatRows=1)
    style = [
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("GRID", (0, 0), (-1, -1), 0.45, GRID),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("LEFTPADDING", (0, 0), (-1, -1), 7),
        ("RIGHTPADDING", (0, 0), (-1, -1), 7),
        ("TOPPADDING", (0, 0), (-1, -1), 6),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
    ]
    for row in range(1, len(data)):
        if row % 2 == 0:
            style.append(("BACKGROUND", (0, row), (-1, row), ROW_ALT))
        if rows[row - 1][0] == "E":
            style.append(("BACKGROUND", (0, row), (-1, row), PALE_RED))
            style.append(("TEXTCOLOR", (0, row), (0, row), RED))
    table.setStyle(TableStyle(style))
    available_h = HEIGHT - 115
    tw, th = table.wrap(WIDTH - 72, available_h)
    table.drawOn(c, 36, HEIGHT - 82 - th)
    page_footer(c)
    c.showPage()


def draw_info_box(c, x, y, w, h, title, lines, fill, border):
    c.setFillColor(fill)
    c.setStrokeColor(border)
    c.roundRect(x, y, w, h, 11, fill=1, stroke=1)
    c.setFillColor(NAVY)
    c.setFont(FONT_BOLD, 12)
    c.drawString(x + 16, y + h - 24, title)
    c.setFont(FONT, 9.5)
    c.setFillColor(TEXT)
    cursor = y + h - 45
    for line in lines:
        c.drawString(x + 17, cursor, line)
        cursor -= 17


def draw_explanation_page(c):
    page_header(
        c,
        "Como se relacionan el AFD y el Lexer",
        "La corrección principal es separar las transiciones del autómata del control de tokens",
        4,
    )
    draw_info_box(c, 36, 475, 545, 270, "1. Reglas formales del AFD", [
        "- Cada flecha consume exactamente un carácter.",
        "- Para un estado y un carácter existe como máximo un destino.",
        "- No existen transiciones lambda.",
        "- Los estados de doble contorno son de aceptación.",
        "- Las clases se mantienen disjuntas: / no pertenece a OP simple.",
        "- CR y LF significan dos caracteres posibles, no una sola entrada CRLF.",
        "- Los prefijos de reservadas son aceptadores porque e, el, pri, etc. son ID.",
        "- Una reservada exacta gana prioridad sólo al finalizar el lexema.",
    ], PALE_BLUE, BLUE)
    draw_info_box(c, 609, 475, 545, 270, "2. Control externo del Lexer", [
        "- El Lexer comienza cada lexema en A.",
        "- Conserva el último estado de aceptación alcanzado.",
        "- Si el siguiente carácter no continúa el lexema, emite el token.",
        "- Luego vuelve a A sin consumir ese carácter pendiente.",
        "- Por eso un espacio despues de var no convierte VAR en ERROR.",
        "- El fin de archivo tampoco es un carácter del alfabeto.",
        "- ERROR se usa para unidades realmente inválidas y su recuperación.",
        "- Espacios y comentarios se reconocen, pero se omiten de la tabla oficial.",
    ], PALE_GREEN, GREEN)

    c.setFillColor(NAVY)
    c.setFont(FONT_BOLD, 15)
    c.drawString(36, 438, "Recorridos de comprobación")
    examples = [
        ("var", "A -v-> M -a-> T -r-> X", "VAR"),
        ("variable", "A -v-> M -a-> T -r-> X -i-> I -(letras)-> I", "ID"),
        ("ifx", "A -i-> K -f-> R -x-> I", "ID"),
        ("12.5", "A -digito-> H -digito-> H -.-> P -digito-> U", "FLOAT"),
        ("12.", "A -> H -> H -> P; fin sin digito -> E", "ERROR: decimal incompleto"),
        ("// nota", "A -/-> G -/-> O -(contenido)-> O", "COMMENT, se omite"),
        ("\"hola\"", "A -comilla-> C -(contenido)-> C -comilla-> N", "STRING"),
    ]
    data = [[p("Entrada", HEADER), p("Recorrido", HEADER), p("Resultado", HEADER)]]
    for entrada, route, result in examples:
        data.append([p(entrada), p(route), p(result)])
    table = Table(data, colWidths=[155, 645, 318])
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("GRID", (0, 0), (-1, -1), 0.45, GRID),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("LEFTPADDING", (0, 0), (-1, -1), 8),
        ("RIGHTPADDING", (0, 0), (-1, -1), 8),
        ("TOPPADDING", (0, 0), (-1, -1), 7),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
        ("BACKGROUND", (0, 2), (-1, 2), ROW_ALT),
        ("BACKGROUND", (0, 5), (-1, 5), PALE_RED),
        ("BACKGROUND", (0, 6), (-1, 6), ROW_ALT),
    ]))
    tw, th = table.wrap(WIDTH - 72, 330)
    table.drawOn(c, 36, 68)
    page_footer(c)
    c.showPage()


def main():
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    c = canvas.Canvas(str(OUTPUT), pagesize=PAGE, pageCompression=1)
    c.setTitle("AFD corregido de MathScriptLexer")
    c.setAuthor("MathScriptLexer")
    draw_graph_page(c)
    draw_state_table(c, STATES_1, "Tabla de estados A-N",
                     "Transiciones consumidas y tratamiento correcto del fin de lexema", 2)
    draw_state_table(c, STATES_2, "Tabla de estados O-AA",
                     "Comentarios, decimales y trie de palabras reservadas", 3)
    draw_explanation_page(c)
    c.save()
    print(OUTPUT)


if __name__ == "__main__":
    main()
