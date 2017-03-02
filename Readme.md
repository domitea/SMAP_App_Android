# SMAP projekt Dominik Matoulek

Tato aplikace je psaná pro předmět SMAP.

Ve složce SMAP je k nahlédnutí zdrojový kód pro Arduino
Zbytek tohoto repozitáře je mobilní aplikace pro Android.

Cílem této dvojice aplikací je inteligentně ovládat krokový motor. K tomu má dopomoct snadné rozhraní mobilní aplikace, která ovládá Arduino skrze Bluetooth sériový port.

## Arduino (Zdrojový kód ve složce SMAP)

Arduino nasdlouchá příkazům. To jsou pole bajtů, kde jeden bajt reprezentuje jeden znak v ASCII formátu. Každý příkaz končí '!'.

Příkazy lze rozdělit do tří základních skupin - Motor, Debug a Home.

### Motor příkazy:

ma{úhel}! - Absolutní natočení motoru o určitý úhel. Absolutní znamená, že se motor snaží dostat k danému natočení nejkratší možnou cestou. V praxi to například znamená, že pokud je zadán příkaz 'ma270!', tak je ve skutečnosti vykonán příkaz 'mr-90!', protože je to rychlejší.

mr{úhel}! - Relativní natočení motoru o určitý úhel. Je to jednoduché a prosté natočení. Pokud mu je zadáno například 'mr720!', motor provede dvě otočky kolem své osy.

### Debug příkazy:

dl! - zapnutí nebo vypnutí zapojené diody

dr! - reset hodnot - 'soft reset', jsou vymazány všechny prac. hodnoty

ds! - vyzkouší fungování krokového motoru tím, že s ním provede jednu otočku

### Home příkazy:

hs! - nastavení domovské pozice motoru

hh! - návrat do domovské pozice motoru

hr! - smazání domovské pozice motoru

Arduino také vrací některé příkazy:

step! - Signalizace kroku detekovaného optozávorou

disc! - Signalizace žádosti o odpojení.

gm! a gp! - Signalizace příkazu kroku pro krokový motor. Slouží pro identifikaci směru kroku získaného z příkazu 'step!'

## Android

Cílem Android aplikace je celou tuto protokolovou komunikaci obalit uživatelsky přívětivým grafickým rozhraním.

Stav: Jsou již plně implementovány Motor, Home příkazy a ovládání motoru pomocí gyroskopu (využit Rotation Vector).

Aktuálně se pracuje na 'step!' signalizaci, která zajistí zobrazení realtime stavu motoru.