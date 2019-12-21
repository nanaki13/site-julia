import { Injectable } from "@angular/core";
import { environment } from "./../environments/environment";
import { Oeuvre, PageElement } from "./model/Oeuvre";
import { of, Observable, interval } from "rxjs";
import { map, catchError } from "rxjs/operators";
import { MessageInternService } from "./message-intern.service";
import { HttpClient } from "@angular/common/http";
import { CacheEnv } from "./util";
import { PageElementDisplay } from "./PageElementDisplay";
@Injectable({
  providedIn: "root"
})
export class OeuvreService {
  private dataFake = new CacheEnv<Oeuvre>(() => new Oeuvre());
  constructor(private http: HttpClient, private ms: MessageInternService) {}

  /**
   * Makes a http get request to retrieve the welcome message from the backend service.
   */
  public oauvres(themeKey: number): Observable<Oeuvre[]> {
    if (environment.online) {
      return this.http
        .get<Oeuvre[]>(`${environment.oeuvreUrl}`, {
          params: { theme_key: `${themeKey}` }
        })
        .pipe(
          catchError(err => {
            console.log("Handling error", err);
            this.ms.push({ content: "Error with server" });
            return this.dataFake
              .asObservable()
              .pipe(map(e => e.filter(pe => pe.themeKey === themeKey)));
          })
        );
    } else {
      debugger;
      return this.dataFake
        .asObservable()
        .pipe(map(e => e.filter(pe => pe.themeKey === themeKey)));
    }
  }

  saveLocalContext() {
    const str = JSON.stringify(this.dataFake.getData());
    console.log(str);
    localStorage.setItem("ov", str);
  }

  restoreLocalContext() {
    const str = localStorage.getItem("ov");
    console.log(str);
    const back = (JSON.parse(str) as Oeuvre[]).map(e => {

      return Object.assign(new Oeuvre(), e);
    });
    this.dataFake.setData(back);
  }

  filterOnThmeKey(themeKey: number) {
    return (pe: PageElement) => pe.themeKey === themeKey;
  }
  public add(title: string, themeKey: number, col: number): Observable<Oeuvre> {
    const o = this.dataFake.newWithId();
    o.themeKey = themeKey;
    o.title = title;
    o.x = col;
    o.y = 0;
    if (environment.online) {
      return this.http.post<Oeuvre>(`${environment.oeuvreUrl}`, o).pipe(
        catchError(err => {
          console.log("Handling error", err);
          this.ms.push({ content: "Error with server" });
          this.dataFake.set(o.id, o);
          return of(o);
        })
      );
    } else {
      this.dataFake.set(o.id, o);
      return of(o);
    }
    return this.http.post<Oeuvre>(`${environment.oeuvreUrl}`, o).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server" });
        this.dataFake.set(o.id, o);
        return of(o);
      })
    );
  }
}
