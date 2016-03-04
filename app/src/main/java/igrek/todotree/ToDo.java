package igrek.todotree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class ToDo {

    /*

    int w,h;
    volatile int stan=0;
    int selected=0, selected_y=0, selected_h=0;
    int scroll = 0;
    int elementsy[] = null;
    String info="",data_save="",edited="";
    int edited_c = 0;
    long key_time = 0;
    int last_key=-1, repeated_i = 0;
    int uppercase = 0, gwiazdka = 0;
    int h_percent = 0, pos_percent = 0, elementsy_h = 0;
    int opcje_pos = 0;
    int copy_from = -1;
    boolean przenoszony = false, last_added = false;
    lista head = null, current = null, clipboard = null;
    //timery
    class MyTimerTask extends TimerTask {
        public void run(){
            time_out();
        }
    }
    class MyTimerTask2 extends TimerTask {
        public void run(){
            clear_info();
            this.cancel();
        }
    }
    Timer timer1 = new Timer();
    MyTimerTask timer1_task = new MyTimerTask();
    Timer timer_list[] = null;
    int timer_list_n = 0;
    long last_time = 0;
    long time_sum = 0;
    
    protected void startApp() {
//        display = Display.getDisplay(this);
//        stan=0;
//        canvas.setFullScreenMode(true);
//        w=canvas.getWidth();
//        h=canvas.getHeight();
//        data_load();
//        display.setCurrent(canvas);
    }

    void paint_lista(Graphics g){
        g.setColor(0x161616);
        g.fillRect(0,0,w,h);
        String content;
        Font activef;
        jebana_java ref = new jebana_java();
        //przesunięcie całościowe
        int y_paint = 41-scroll;
        //tło pod selected
        selected_y = y_paint-3;
        for(int i=0; i<selected; i++) selected_y+=elementsy[i];
        selected_h = elementsy[selected];
        g.setColor(0x0a4178);
        if(przenoszony) g.setColor(0x555555);
        g.fillRect(0,selected_y,w,selected_h);
        //kreski oddzielające
        selected_y = y_paint-3;
        g.setColor(0x272727);
        for(int i=0; i<current.in_n+1; i++){
            selected_y+=elementsy[i];
            g.drawLine(0,selected_y,w,selected_y);
        }
        //scrollbar
        elementsy_h = Config.line_height;
        for(int i=0; i<current.in_n; i++){
            elementsy_h+=elementsy[i];
        }
        g.setColor(0x303030);
        g.fillRect(w-2,Config.title_h,2,h-Config.tit_st_h);
        if(elementsy_h>h-Config.tit_st_h){
            h_percent = (h-Config.tit_st_h)*(h-Config.tit_st_h)/elementsy_h;
            if(h_percent>(h-Config.tit_st_h)) h_percent=(h-Config.tit_st_h);
            pos_percent = (scroll*(h-Config.tit_st_h))/elementsy_h;
        }else{
            h_percent = h-Config.tit_st_h;
            pos_percent=0;
        }
        g.setColor(0x1c4768);
        g.fillRect(w-2,Config.title_h+pos_percent,2,h_percent);
        //lista elementów
        g.setColor(0xf0f0f0);
        for(int i=0; i<current.in_n; i++){
            content=""+current.in[i].text;
            if(current.in[i].in_n>0){
                content+=" ["+current.in[i].in_n+"]";
                activef = f2;
            }else{
                activef = f1;
            }
            g.setFont(activef);
            ref.ref_int = y_paint;
            draw_string_multiline(g,activef,content,0,ref,Graphics.TOP|Graphics.LEFT);
            y_paint += elementsy[i];
            if(y_paint>h) break;
        }
        //przycisk dodawania nowego elementu
        g.setColor(0x909090);
        g.setFont(f2);
        g.drawString("+",w/2,y_paint,Graphics.TOP|Graphics.HCENTER);
        y_paint += Config.line_height;
        //tło tytułu listy
        g.setColor(0x15304d);
        g.fillRect(0,0,w,Config.title_h);
        g.fillRect(0,h-Config.stopka_h,w,Config.stopka_h); //i stopka
        //sygnaturka
        g.setFont(f2);
        g.setColor(0x25405d);
        g.drawString("Igrek",2,0,Graphics.TOP|Graphics.LEFT);
        //tytuł
        y_paint = 9;
        g.setColor(0xdbdcf7);
        g.drawString(current.text,w/2,y_paint,Graphics.TOP|Graphics.HCENTER);
        //numer wybranego elementu
        g.setColor(0x2e68a9);
        g.drawString((selected<current.in_n?""+(selected+1):"")+"/"+current.in_n+"",w-5,y_paint,Graphics.TOP|Graphics.RIGHT);
        //przyciski na dole
        g.setColor(0xf0f0f0);
        if(przenoszony){
            g.drawString("OK",w/2,h,Graphics.BOTTOM|Graphics.HCENTER);
        }else{
            g.drawString((selected==current.in_n)?"Dodaj":"Edytuj",w/2,h,Graphics.BOTTOM|Graphics.HCENTER);
            g.drawString("Opcje",0,h,Graphics.BOTTOM|Graphics.LEFT);
            g.drawString("Wyjdź",w,h,Graphics.BOTTOM|Graphics.RIGHT);
        }
        //info
        paint_info(g);
    }

    void paint_edycja(Graphics g){
        g.setColor(0x242424);
        g.fillRect(0,0,w,h);
        int y_paint = 39;
        int letters_in_line = w/Config.letter_w;
        //początek zaznaczenia
        if(copy_from >= 0){ 
            g.setColor(0x404040);
            g.fillRect((copy_from%letters_in_line)*Config.letter_w,y_paint+(copy_from/letters_in_line)*Config.letter_h,Config.letter_w,Config.letter_h);
        }
        //kursor
        g.setColor(0x0e58b4);
        int cursor = edited_c;
        if(last_key!=-1&&edited_c>0) cursor--;
        g.fillRect((cursor%letters_in_line)*Config.letter_w,y_paint+(cursor/letters_in_line)*Config.letter_h,Config.letter_w,Config.letter_h);
        //edytowany tekst
        g.setFont(f1);
        g.setColor(0xe2e2e2);
        //aktualne pisanie spacji
        if(last_key==0&&repeated_i==0){
            g.drawChar('_',(cursor%letters_in_line)*Config.letter_w+Config.letter_w/2,y_paint+(cursor/letters_in_line)*Config.letter_h,Graphics.TOP|Graphics.HCENTER);
        }
        for(int i=0; i<edited.length(); i++){
            g.drawChar(edited.charAt(i),(i%letters_in_line)*Config.letter_w+Config.letter_w/2,y_paint+(i/letters_in_line)*Config.letter_h,Graphics.TOP|Graphics.HCENTER);
        }
        y_paint += Config.letter_h*((edited.length()-1)/letters_in_line+1);
        //tło tytułu listy
        g.setColor(0x15304d);
        g.fillRect(0,0,w,Config.title_h);
        g.fillRect(0,h-Config.stopka_h,w,Config.stopka_h); //i stopka
        y_paint = 9;
        g.setFont(f2);
        //sygnaturka
        g.setColor(0x25405d);
        g.drawString("Igrek",2,0,Graphics.TOP|Graphics.LEFT);
        //tytuł
        g.setColor(0xdbdcf7);
        g.drawString("["+current.text+"]",w/2,y_paint,Graphics.TOP|Graphics.HCENTER);
        //uppercase/lowercase
        g.setColor(0x2e68a9);
        g.drawString(uppercase==0?"abc":(uppercase==1?"Abc":(uppercase==2?"ABC":"123")),w-5,y_paint,Graphics.TOP|Graphics.RIGHT);
        //przyciski na dole
        g.setColor(0xf0f0f0);
        g.drawString("Zapisz",w/2,h,Graphics.BOTTOM|Graphics.HCENTER);
        g.drawString("Wróć",w,h,Graphics.BOTTOM|Graphics.RIGHT);
        //przycisk obliczania formuły
        if(Parser.is_formula(edited)){
            g.drawString("Oblicz",0,h,Graphics.BOTTOM|Graphics.LEFT);
        }
        //info
        paint_info(g);
    }
    void paint_gwiazdka(Graphics g){
        g.setColor(0x2F2F2F);
        g.fillRect(0,0,w,h);
        int letters_in_line = w/Config.gwiazdka_w;
        //kursor
        g.setColor(0x0e58b4);
        g.fillRect((gwiazdka%letters_in_line)*Config.gwiazdka_w,(gwiazdka/letters_in_line)*Config.gwiazdka_h,Config.gwiazdka_w,Config.gwiazdka_h);
        //siatka
        g.setColor(0x393939);
        for(int i=1; i<(Config.znaki_specjalne.length-1)/letters_in_line+2; i++){
            g.drawLine(0,i*Config.gwiazdka_h,w,i*Config.gwiazdka_h);
        }
        for(int i=1; i<letters_in_line; i++){
            g.drawLine(i*Config.gwiazdka_w,0,i*Config.gwiazdka_w,((Config.znaki_specjalne.length-1)/letters_in_line+1)*Config.gwiazdka_h);
        }
        //tablica znaków
        g.setFont(f2);
        g.setColor(0xe2e2e2);
        for(int i=0; i<Config.znaki_specjalne.length; i++){
            g.drawChar(Config.znaki_specjalne[i],(i%letters_in_line)*Config.gwiazdka_w+Config.gwiazdka_w/2,6+(i/letters_in_line)*Config.gwiazdka_h,Graphics.TOP|Graphics.HCENTER);
        }
        //tło stopki
        g.setColor(0x15304d);
        if(clipboard==null){
            g.fillRect(0,h-Config.stopka_h*2,w,Config.stopka_h*2);
        }else{
            g.fillRect(0,h-Config.stopka_h*3,w,Config.stopka_h*3);
        }
        g.setFont(f2);
        //przyciski na dole
        g.setColor(0xf0f0f0);
        if(clipboard!=null){
            g.drawString("* - Wklej ze schowka",0,h-Config.stopka_h*2,Graphics.BOTTOM|Graphics.LEFT);
        }
        //zaznaczanie do skopiowania
        if(copy_from >= 0){ 
            g.drawString("# - Kopiuj do",0,h-Config.stopka_h,Graphics.BOTTOM|Graphics.LEFT);
        }else{
            g.drawString("# - Kopiuj od",0,h-Config.stopka_h,Graphics.BOTTOM|Graphics.LEFT);
        }
        g.drawString("Dodaj",0,h,Graphics.BOTTOM|Graphics.LEFT);
        g.drawString("Wybierz",w/2,h,Graphics.BOTTOM|Graphics.HCENTER);
        g.drawString("Wróć",w,h,Graphics.BOTTOM|Graphics.RIGHT);
        //info
        paint_info(g);
    }
    void paint_opcje(Graphics g){
        g.setColor(0x242424);
        g.fillRect(0,0,w,h);
        //opcje_pos
        g.setColor(0x333333);
        g.fillRect(0,41+opcje_pos*Config.line_height,w,Config.line_height);
        //lista opcji
        g.setFont(f2);
        g.setColor(0x909090);
        int y_paint = 42;
        g.drawString("> Przenieś element [#]",w/2,y_paint,Graphics.TOP|Graphics.HCENTER); y_paint+=Config.line_height;
        g.drawString("> Kopiuj element",w/2,y_paint,Graphics.TOP|Graphics.HCENTER); y_paint+=Config.line_height;
        g.drawString("> Wklej element",w/2,y_paint,Graphics.TOP|Graphics.HCENTER); y_paint+=Config.line_height;
        g.drawString("> Usuń element",w/2,y_paint,Graphics.TOP|Graphics.HCENTER); y_paint+=Config.line_height;
        g.drawString("> Minimalizuj",w/2,y_paint,Graphics.TOP|Graphics.HCENTER); y_paint+=Config.line_height;
        g.drawString("> Wczytaj bazę danych",w/2,y_paint,Graphics.TOP|Graphics.HCENTER); y_paint+=Config.line_height;
        g.drawString("> Zapisz bazę danych",w/2,y_paint,Graphics.TOP|Graphics.HCENTER); y_paint+=Config.line_height;
        g.drawString("> Zapisz i wyjdź",w/2,y_paint,Graphics.TOP|Graphics.HCENTER); y_paint+=Config.line_height;
        g.drawString("> Wyjdź bez zapisywania",w/2,y_paint,Graphics.TOP|Graphics.HCENTER); y_paint+=Config.line_height;
        //tło tytułu listy
        g.setColor(0x15304d);
        g.fillRect(0,0,w,Config.title_h);
        g.fillRect(0,h-Config.stopka_h,w,Config.stopka_h); //i stopka
        //sygnaturka
        g.setColor(0x25405d);
        g.drawString("Igrek",2,0,Graphics.TOP|Graphics.LEFT);
        //tytuł
        g.setColor(0x2e68a9);
        g.drawString("Opcje",w/2,9,Graphics.TOP|Graphics.HCENTER);
        //przyciski na dole
        g.setColor(0xf0f0f0);
        g.drawString("Wykonaj",w/2,h,Graphics.BOTTOM|Graphics.HCENTER);
        g.drawString("Wróć",w,h,Graphics.BOTTOM|Graphics.RIGHT);
        //info
        paint_info(g);
    }
    void paint_zamykanie(Graphics g){
        g.setColor(0x000000);
        g.fillRect(0,0,w,h);
        g.setFont(f2);
        //sygnaturka
        g.setColor(0x202020);
        g.drawString("Igrek",2,0,Graphics.TOP|Graphics.LEFT);
        g.setColor(0xe0e0e0);
        g.drawString("Zamykanie...",w/2,h/2,Graphics.TOP|Graphics.HCENTER);
    }
    void paint_info(Graphics g){
        if(info.length()>0){
            g.setFont(f1);
            int x_paint=w;
            int y_paint = h-22;//-get_info_h();
            char ch;
            //g.setColor(0x122841);
            //g.fillRect(0,y_paint-3,w,get_info_h()+4);
            g.setColor(0x4079bf);
            for(int i=info.length()-1; i>=0&&i<info.length(); i--){
                ch = info.charAt(i);
                if(ch=='\n'){
                    x_paint=w;
                    y_paint-=Config.letter_h;
                    continue;
                }
                g.drawChar(ch,x_paint,y_paint,Graphics.BOTTOM|Graphics.RIGHT);
                x_paint-=f1.charWidth(ch);
            }
        }
    }
    
    int[] elementsy_size(){
        int[] result = new int[current.in_n+1];
        String content;
        Font activef;
        for(int i=0; i<current.in_n; i++){
            content=""+current.in[i].text;
            if(current.in[i].in_n>0){
                content+=" ["+current.in[i].in_n+"]";
                activef = f2;
            }else{
                activef = f1;
            }
            result[i] = get_string_h(activef,content);
        }
        result[current.in_n] = 21; //wysokość plusa
        return result;
    }
    int get_string_h(Font activef, String s){
        int result = 0;
        for(int i=0; i<s.length()-1; i++){
            if(activef.stringWidth(s.substring(0,i+1)+"-")>w){
                result += 16;
                return result+get_string_h(activef,s.substring(i,s.length()));
            }
        }
        return result + Config.line_height;
    }
    void draw_string_multiline(Graphics g, Font activef, String s, int x, jebana_java ref, int align){
        if(ref.ref_int>h) return;
        for(int i=0; i<s.length()-1; i++){
            if(activef.stringWidth(s.substring(0,i+1)+"-")>w){
                g.drawString(s.substring(0,i)+"-",x,ref.ref_int,align);
                ref.ref_int+=16;
                draw_string_multiline(g,activef,s.substring(i,s.length()),x,ref,align);
                return;
            }
        }
        g.drawString(s,x,ref.ref_int,align);
        ref.ref_int+=Config.line_height;
    }
    
    boolean file_exists(String path){
        try{
            fc = (FileConnection) Connector.open("file://localhost/"+path);
            if(fc.exists()){
                fc.close();
                return true;
            }
            fc.close();
        } catch (IOException ex) { error_out(ex); }
        return false;
    }
    
    String open_file(String path){
        String bajty_s="";
        try{
            fc = (FileConnection) Connector.open("file://localhost/"+path);
            if(!fc.exists()){
                error_out("Plik "+path+" nie istnieje!");
                fc.close();
            }else{
                int length=(int)fc.fileSize();
                InputStream fis = fc.openInputStream();
                byte bajty[] = new byte[length];
                fis.read(bajty, 0, length);
                bajty_s = new String(bajty, 0, length);
                fis.close();
                fc.close();
            }
        } catch (IOException ex) { error_out(ex); }
        return bajty_s;
    }
    void load_element(lista parent, String data){
        String wiersz="";
        lista last = parent;
        char ch;
        for(int i=0; i<data.length(); i++){
            ch = data.charAt(i);
            if(ch=='\r') ch='\n';
            if(ch=='\t') continue;
            //kodowanie ANSI
            ch = Encoding.ansi_to_polish(ch);
            if(ch=='\n'){
                if(wiersz.length()>0){
                    //analizuj wiersz
                    if(wiersz.compareTo("{")!=0&&wiersz.compareTo("}")!=0){
                        parent.add(wiersz);
                        last = parent.in[parent.in_n-1];
                    }else if(wiersz.compareTo("{")==0){
                        //szukaj końca nawiasu
                        int nawias2 = 1, nawias_pos=0;
                        for(int j=i+1; j<data.length(); j++){
                            if(data.charAt(j)=='{') nawias2++;
                            if(data.charAt(j)=='}') nawias2--;
                            if(nawias2==0){
                                nawias_pos=j;
                                break;
                            }
                        }
                        if(nawias2!=0){
                            error_out("Brak nawiasu domykającego \"}\"!");
                            return;
                        }
                        String new_data = data.substring(i,nawias_pos);
                        i=nawias_pos;
                        load_element(last,new_data);
                    }
                }
                wiersz="";
            }else{
                wiersz+=ch;
            }
        }
                
    }
    void data_load(){
        head = new lista();
        head.text = "root";
        if(!file_exists(Config.data_path)){
            error_out("Brak pliku z danymi!");
            change_current(0);
            return;
        }
        String data = open_file(Config.data_path);
        if(data.length()==0) return;
        load_element(head,data);
        change_current(0);
        error_out("Wczytano bazę danych.");
    }
    
    void write_file(String path, byte[] data) {
        try {
            fc = (FileConnection) Connector.open("file://localhost/"+path, Connector.READ_WRITE);
            if(!fc.exists()){
                error_out("Tworzenie nowego pliku");
                fc.create();
            }else{
                fc.truncate(0);
            }
            OutputStream os = fc.openOutputStream();
            os.write(data);
            os.flush();
            os.close();
            fc.close();
        } catch (IOException ex) {
            error_out(ex);
        }
    }
    void save_element(lista e, int level){
        String wciecia = "";
        for(int i=0; i<level; i++) wciecia += "\t";
        data_save += wciecia + e.text + "\r\n";
        if(e.in_n>0){
            data_save += wciecia + "{\r\n";
            for(int i=0; i<e.in_n; i++){
                save_element(e.in[i], level+1);
            }
            data_save += wciecia + "}\r\n";
        }
    }
    void data_save(){
        //zbieranie danych
        data_save="";
        for(int i=0; i<head.in_n; i++){
            save_element(head.in[i],0);
        }
        //zamiana na kodowanie cp-1250
        byte[] bajciki = new byte [data_save.length()];
        for(int i=0; i<bajciki.length; i++){
            bajciki[i] = Encoding.polish_to_ansi(data_save.charAt(i));
        }
        //zapis do pliku
        write_file(Config.data_path,bajciki);
        error_out("Zapisano bazę danych.");
    }
    
    void change_current(int inc){
        if(inc==0){
            current = head;
            selected = 0;
        }else if(inc==-1){
            if(current.parent!=null){
                lista old_current = current;
                current=current.parent;
                selected = 0;
                //znajdź samego siebie
                for(int i=0; i<current.in_n; i++){
                    if(current.in[i]==old_current){
                        selected=i;
                        break;
                    }
                }
                scroll = 0;
            }else{
                error_out("Brak obiektu nadrzędnego!");
            }
        }else if(inc==+1){
            if(selected<current.in_n){
                current = current.in[selected];
                selected=0;
                scroll = 0;
            }else{
                error_out("Nie wybrano elementu!");
            }
        }
        //obliczenie wysokości elementów
        elementsy = elementsy_size();
        przenoszony = false;
        change_selected(0);
    }
    void change_selected(int inc){
        selected+=inc;
        if(selected<0) selected=current.in_n;
        if(selected>current.in_n) selected=0;
        update_scroll();
    }
    void update_scroll(){
        selected_y = 41-scroll;
        for(int i=0; i<selected; i++) selected_y+=elementsy[i];
        selected_h = elementsy[selected];
        //górna granica: 37
        if(selected_y<37){
            scroll = 0;
            for(int i=0; i<selected; i++) scroll+=elementsy[i];
        }else if(selected_y+selected_h>h-21){ //dolna granica: h-21
            scroll += selected_y+selected_h - h+21;
        }
    }
    
    void new_element(){
        stan = 1;
        current.add("");
        lista nowy = current.in[current.in_n-1];
        for(int i=current.in_n-1; i>selected; i--){
            current.in[i] = current.in[i-1];
        }
        current.in[selected] = nowy;
        edit_element();
        last_added = true;
        error_out("Utworzono nowy element.");
    }
    void new_element(int input){
        new_element();
        input_insert_char(get_key_char(input));
        last_key = input;
        timer1.cancel();
        timer1 = new Timer();
        timer1_task = new MyTimerTask();
        timer1.schedule(timer1_task,Config.key_delay);
    }
    void edit_element(){
        if(selected==current.in_n) return;
        last_added = false;
        edited = current.in[selected].text;
        edited_c = current.in[selected].text.length();
        reset_key();
        uppercase = 0;
        copy_from = -1;
        stan = 1;
    }
    void discard_edited(){
        stan = 0;
        error_out("Porzucono zmiany.");
        if(current.in[selected].text.length()==0) delete_element();
        change_current(2);
    }
    void save_edited(){
        current.in[selected].text = biale_znaki(edited);
        stan = 0;
        if(current.in[selected].text.length()==0){
            error_out("Zawartość jest pusta.");
            delete_element();
            return;
        }
        if(last_added&&selected==current.in_n-1) selected++;
        change_current(2);
        error_out("Zmiany zostały zapisane.");
    }
    void delete_element(){
        if(selected==current.in_n){
            error_out("Nie wybrano elementu!");
            return;
        }
        current.delete(selected);
        if(selected==current.in_n&&current.in_n>0){ //jeśli był to ostatni element i istnieją elementy
            selected = current.in_n-1; //zaznaczenie ostatniego
        }
        change_current(2);
        error_out("Element został usunięty.");
    }
    
    String biale_znaki(String s){
        boolean zmiana = false;
        if(s.length()>0){
            while(s.charAt(0)==' '){ //pierwsze znaki
                s = s.substring(1,s.length());
                zmiana = true;
                if(s.length()==0) break;
            }
        }
        if(s.length()>0){
            while(s.charAt(s.length()-1)==' '){ //ostatnie znaki
                s = s.substring(0,s.length()-1);
                zmiana = true;
                if(s.length()==0) break;
            }
        }
        if(zmiana) error_out("Usunięto białe znaki.");
        return s;
    }
    
    void input_edited(int input){
        if(input==-1){ //backspace
            if(edited_c>0){
                edited = edited.substring(0,edited_c-1) + edited.substring(edited_c,edited.length());
                edited_c--;
                input_move(0);
            }
            return;
        }
        if(input==11){ //krzyżyk - uppercase
            uppercase++;
            if(uppercase>3) uppercase=0;
            reset_key();
            timer1.cancel();
            timer1 = new Timer();
            timer1_task = new MyTimerTask();
            return;
        }
        if(input==10){ //gwiazdka - wybór znaków
            reset_key();
            //gwiazdka = 0;
            stan = 2;
            return;
        }
        //same cyfry
        if(uppercase==3){
            input_insert_char((char)((int)'0'+input));
            reset_key();
            return;
        }
        if(input==last_key){ //powtórzenie tego samego klawisza
            repeated_i++;
            //zmiana aktualnego znaku na następny
            if(edited_c>0){
                char c = get_key_char(input);
                if(uppercase>0) c = Encoding.to_uppercase(c);
                edited = edited.substring(0,edited_c-1) + c + edited.substring(edited_c,edited.length());
            }
        }else{
            //dodanie nowego znaku
            repeated_i=0;
            if(uppercase==1&&last_key!=-1) uppercase = 0;
            input_insert_char(get_key_char(input));
            last_key = input;
        }
        timer1.cancel();
        timer1 = new Timer();
        timer1_task = new MyTimerTask();
        timer1.schedule(timer1_task,Config.key_delay);
    }
    void input_insert_char(char c){
        if(uppercase>0) c = Encoding.to_uppercase(c);
        edited = edited.substring(0,edited_c) + c + edited.substring(edited_c,edited.length());
        edited_c++;
    }
    void input_insert_repeated(char c){
        input_edited(-1);
        input_insert_char(c);
        reset_key();
        canvas.repaint();
    }
    void input_move(int move){
        //zakończenie poprzedniego pisania
        if(last_key!=-1){
            reset_key();
            if(uppercase==1) uppercase = 0;
            if(move!=-1) return;
        }
        edited_c+=move;
        if(((move>0)?move:-move)>1){ //skok o więcej niż 1
            if(edited_c<0) edited_c = 0;
            if(edited_c>edited.length()) edited_c = edited.length();
        }
        if(edited_c<0) edited_c=edited.length();
        if(edited_c>edited.length()) edited_c=0;
    }
    void reset_key(){
        repeated_i = 0;
        last_key = -1;
    }
    void time_out(){
        reset_key();
        if(uppercase==1) uppercase = 0;
        canvas.repaint();
    }
    
    char get_key_char(int input){
        int tab_size=1;
        if(input==0) tab_size = 2;
        if(input==1) tab_size = 13;
        if(input==2) tab_size = 6;
        if(input==3) tab_size = 5;
        if(input==4) tab_size = 4;
        if(input==5) tab_size = 5;
        if(input==6) tab_size = 6;
        if(input==7) tab_size = 6;
        if(input==8) tab_size = 4;
        if(input==9) tab_size = 7;
        repeated_i = repeated_i%tab_size;
        char c = '0';
        if(input==0){ //0
            if(repeated_i == 0) c = ' ';
            if(repeated_i == 1) c = '0';
        }
        if(input==1){ //1
            if(repeated_i == 0) c = '.';
            if(repeated_i == 1) c = ',';
            if(repeated_i == 2) c = '?';
            if(repeated_i == 3) c = '!';
            if(repeated_i == 4) c = '1';
            if(repeated_i == 5) c = '@';
            if(repeated_i == 6) c = '\'';
            if(repeated_i == 7) c = '-';
            if(repeated_i == 8) c = '_';
            if(repeated_i == 9) c = '(';
            if(repeated_i == 10) c = ')';
            if(repeated_i == 11) c = ':';
            if(repeated_i == 12) c = ';';
        }
        if(input==2){ //2
            if(repeated_i == 0) c = 'a';
            if(repeated_i == 1) c = 'ą';
            if(repeated_i == 2) c = 'b';
            if(repeated_i == 3) c = 'c';
            if(repeated_i == 4) c = 'ć';
            if(repeated_i == 5) c = '2';
        }
        if(input==3){ //3
            if(repeated_i == 0) c = 'd';
            if(repeated_i == 1) c = 'e';
            if(repeated_i == 2) c = 'ę';
            if(repeated_i == 3) c = 'f';
            if(repeated_i == 4) c = '3';
        }
        if(input==4){ //4
            if(repeated_i == 0) c = 'g';
            if(repeated_i == 1) c = 'h';
            if(repeated_i == 2) c = 'i';
            if(repeated_i == 3) c = '4';
        }
        if(input==5){ //5
            if(repeated_i == 0) c = 'j';
            if(repeated_i == 1) c = 'k';
            if(repeated_i == 2) c = 'l';
            if(repeated_i == 3) c = 'ł';
            if(repeated_i == 4) c = '5';
        }
        if(input==6){ //6
            if(repeated_i == 0) c = 'm';
            if(repeated_i == 1) c = 'n';
            if(repeated_i == 2) c = 'ń';
            if(repeated_i == 3) c = 'o';
            if(repeated_i == 4) c = 'ó';
            if(repeated_i == 5) c = '6';
        }
        if(input==7){ //7
            if(repeated_i == 0) c = 'p';
            if(repeated_i == 1) c = 'q';
            if(repeated_i == 2) c = 'r';
            if(repeated_i == 3) c = 's';
            if(repeated_i == 4) c = 'ś';
            if(repeated_i == 5) c = '7';
        }
        if(input==8){ //8
            if(repeated_i == 0) c = 't';
            if(repeated_i == 1) c = 'u';
            if(repeated_i == 2) c = 'v';
            if(repeated_i == 3) c = '8';
        }
        if(input==9){ //9
            if(repeated_i == 0) c = 'w';
            if(repeated_i == 1) c = 'x';
            if(repeated_i == 2) c = 'y';
            if(repeated_i == 3) c = 'z';
            if(repeated_i == 4) c = 'ź';
            if(repeated_i == 5) c = 'ż';
            if(repeated_i == 6) c = '9';
        }
        return c;
    }
    
    void gwiazdka_move(int move){
        gwiazdka+=move;
        if(((move>0)?move:-move)>1){
            if(gwiazdka<0) gwiazdka = 0;
            if(gwiazdka>Config.znaki_specjalne.length-1) gwiazdka = Config.znaki_specjalne.length-1;
        }
        if(gwiazdka<0) gwiazdka = Config.znaki_specjalne.length-1;
        if(gwiazdka>Config.znaki_specjalne.length-1) gwiazdka = 0;
    }
    void gwiazdka_input(int input){
        if(input==0){ //wróć
            stan=1;
        }else if(input==1){ //wybierz i wróć
            input_insert_char(Config.znaki_specjalne[gwiazdka]);
            stan=1;
        }else if(input==2){ //dodaj
            input_insert_char(Config.znaki_specjalne[gwiazdka]);
            error_out("Dodano znak: "+Config.znaki_specjalne[gwiazdka]);
        }
    }
    
    void pokaz_opcje(){
        opcje_pos = 0;
        stan=3;
    }
    void opcje_move(int move){
        opcje_pos+=move;
        if(opcje_pos>8) opcje_pos = 0;
        if(opcje_pos<0) opcje_pos = 8;
    }
    void opcje_wykonaj(){
        stan=0;
        if(opcje_pos==0){ //przenieś
            przenies(true);
        }else if(opcje_pos==1){ //kopiuj
            element_copy();
        }else if(opcje_pos==2){ //wklej
            element_paste();
        }else if(opcje_pos==3){ //usuń
            delete_element();
        }else if(opcje_pos==4){ //minimalizuj
            minimize_app();
        }else if(opcje_pos==5){ //wczytaj bazę
            data_load();
        }else if(opcje_pos==6){ //zapisz bazę
            data_save();
        }else if(opcje_pos==7){ //zapisz i wyjdź
            save_and_exit();
        }else if(opcje_pos==8){ //wyjdź bez zapisywania
            exit();
        }
    }
    
    void element_copy(){
        if(selected==current.in_n){
            error_out("Nie wybrano elementu!");
            return;
        }
        clipboard = lista_copy(current.in[selected]);
        error_out("Skopiowano element.");
    }
    lista lista_copy(lista original){
        lista kopia = new lista();
        kopia.text = original.text;
        kopia.in_n = original.in_n;
        kopia.in = new lista [kopia.in_n];
        for(int i=0; i<kopia.in_n; i++){
            kopia.in[i] = lista_copy(original.in[i]);
            kopia.in[i].parent = kopia;
        }
        return kopia;
    }
    void element_paste(){
        if(clipboard==null){
            error_out("Schowek jest pusty!");
            return;
        }
        current.add("");
        for(int i=current.in_n-1; i>selected; i--){ //przesunięcie elementów po zaznaczeniu
            current.in[i] = current.in[i-1];
        }
        current.in[selected] = clipboard;
        current.in[selected].parent = current;
        change_current(2);
        clipboard = null;
        error_out("Wklejono nowy element.");
    }
    void text_paste(){
        if(clipboard==null) return;
        String to_insert = clipboard.text;
        edited = edited.substring(0,edited_c) + to_insert + edited.substring(edited_c,edited.length());
        edited_c += to_insert.length();
        stan = 1;
        error_out("Wklejono tekst ze schowka.");
    }
    void text_copy(){
        stan = 1;
        if(copy_from==-1){ //wybieranie punktu początkowego
            copy_from = edited_c;
            error_out("Wybrano punkt początkowy");
        }else{ //wybieranie punktu końcowego
            //sprawdzanie, czy początek mieści się w stringu
            if(copy_from > edited.length()){
                copy_from = edited_c;
                error_out("Błąd - nowy punkt początkowy");
                return;
            }
            //puste zaznaczenie
            int copy_end = edited_c;
            if(copy_from == copy_end){
                error_out("Brak zaznaczenia");
                return;
            }
            //punkt początkowy za końcowym
            if(copy_from > copy_end){
                //odwrócenie zaznaczenia
                copy_end = copy_from;
                copy_from = edited_c;
            }
            //utworzenie obiektu z tekstem
            clipboard = new lista();
            clipboard.text = edited.substring(copy_from, copy_end);
            copy_from = -1; //wyzerowanie zaznaczania
            error_out("Skopiowano: "+clipboard.text);
        }
    }
    
    void przenies(boolean p){
        if(p==true){
            if(selected==current.in_n){
                error_out("Nie wybrano elementu!");
                return;
            }
            przenoszony = true;
            error_out("Przenieś element i naciśnij OK.");
        }else{
            przenoszony = false;
            error_out("Przenoszenie zakończone.");
        }
    }
    void przenies_element(int move){
        if(current.in_n<=1) return;
        int zamieniany = selected+move;
        lista temp = current.in[selected];
        if(move==0){ //przenieś na początek
            for(int i=selected; i>0; i--){
                current.in[i] = current.in[i-1];
            }
            current.in[0] = temp;
            selected = 0;
        }else if(move==2){ //przenieś na koniec
            for(int i=selected; i<current.in_n-1; i++){
                current.in[i] = current.in[i+1];
            }
            current.in[current.in_n-1] = temp;
            selected = current.in_n-1;
        }else if(zamieniany<0){ //selected - pierwszy element
            for(int i=0; i<current.in_n-1; i++){
                current.in[i] = current.in[i+1];
            }
            current.in[current.in_n-1] = temp;
            selected = current.in_n-1;
        }else if(zamieniany>=current.in_n){ //selected - ostatni element
            for(int i=current.in_n-1; i>0; i--){
                current.in[i] = current.in[i-1];
            }
            current.in[0] = temp;
            selected = 0;
        }else{
            current.in[selected] = current.in[zamieniany];
            current.in[zamieniany] = temp;
            selected = zamieniany;
        }
        elementsy = elementsy_size();
        change_selected(0);
    }
    
    void save_and_exit(){
        stan = 4;
    }
    
    void calculate_formula(){
        if(!Parser.is_formula(edited)) return;
        String formula = edited;
        //pierwsze wystąpienie tekstu (formuła+tekst)
        int text_pos = formula.length();
        for(int i=0; i<formula.length(); i++){
            char c = formula.charAt(i);
            if(Parser.is_number(c) || Parser.is_operator(c) || c==',' || c==' '){
                continue;
            }
            text_pos = i;
            break;
        }
        //spacja między formułą a tekstem
        if(text_pos > 0){
            if(formula.charAt(text_pos-1)==' '){ //jeśli poprzedni znak jest spacją
                text_pos--; //przemieść go do sekcji z tekstem
            }
        }
        String non_formula = formula.substring(text_pos); //tekst za formułą
        formula = formula.substring(0,text_pos);
        //formatowanie: przecinki, spacje
        formula = Parser.formula_format(formula);
        try {
            //lista segmentów
            Vector segmenty = Parser.segmentuj(formula);
            //sprawdzenie poprawności segmentów
            Parser.segments_verify(segmenty);
            //oblicz wynik
            edited = Parser.segments_calculate(segmenty) + non_formula;
        } catch (StringException ex) {
            error_out(ex);
            return;
        }
        edited_c = edited.length();
        reset_key();
        error_out("Obliczono wyrażenie: "+formula);
    }
    
    void error_out(Exception ex){
        error_out(ex.toString());
    }
    void error_out(StringException ex){
        error_out(ex.dupa);
    }
    void error_out(String ex){
        for(int i=0; i<ex.length()-1; i++){
            if(f1.stringWidth(ex.substring(0,i+1))>w){
                error_out(ex.substring(i,ex.length()));
                ex = ex.substring(0,i);
                break;
            }
        }
        info="\n"+ex+info;
        Timer timer_info = new Timer();
        MyTimerTask2 timer2_task = new MyTimerTask2();
        timer_info.schedule(timer2_task, 3000);
        Timer nowa_tablica[] = new Timer [timer_list_n+1];
        for(int i=0; i<timer_list_n; i++){
            nowa_tablica[i] = timer_list[i];
        }
        nowa_tablica[timer_list_n] = timer_info;
        timer_list = nowa_tablica;
        timer_list_n++;
    }
    int get_info_h(){
        int ns=0;
        for(int i=0; i<info.length(); i++){
            if(info.charAt(i)=='\n') ns++;
        }
        return ns*Config.letter_h;
    }
    void clear_info(){
        //usunięcie timera
        if(timer_list_n>0){
            timer_list[0].cancel();
            timer_list_n--;
            Timer nowa_tablica[] = new Timer [timer_list_n];
            for(int i=0; i<timer_list_n; i++){
                nowa_tablica[i] = timer_list[i+1];
            }
            timer_list = nowa_tablica;
        }
        int lastn = -1;
        for(int i=0; i<info.length(); i++){
            if(info.charAt(i)=='\n') lastn = i;
        }
        if(lastn<=0) info="";
        else info = info.substring(0,lastn);
        canvas.repaint();
    }
    
    void minimize_app(){
        display.setCurrent(null);
    }
    void exit(){
        destroyApp(false);
    }
    public ToDo() { }
    public void pauseApp() { }
    public void resumeApp() {
        display.setCurrent(canvas);
    }
    public void destroyApp(boolean unconditional) { notifyDestroyed(); }

    public void commandAction(Command c, Displayable d) { }

    */
}