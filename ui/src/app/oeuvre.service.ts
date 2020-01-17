import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, of } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { environment } from "./../environments/environment";
import { Service } from "./image-view/image-view.component";
import { MessageInternService } from "./message-intern.service";
import { Oeuvre } from "./model/Oeuvre";
import { PageElement } from "./model/PageElement";
import { CacheEnv } from "./CacheEnv";
@Injectable({
  providedIn: "root"
})
export class OeuvreService implements Service {
  private dataFake = new CacheEnv<Oeuvre>(() => new Oeuvre());
  constructor(private http: HttpClient, private ms: MessageInternService) {}

  private _currentItem = new Oeuvre({id : 0, title : "" ,description : "",dimensionX : 0, dimensionY : 0,creation : 2020});

  get currentItem() {return this._currentItem;}

  update(o: Oeuvre): Observable<boolean> {
    o.updateStatus = undefined;
    o.updated = undefined;
    return this.http.patch<any>(environment.oeuvreUrl, o).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server, see log" });
        return of(false);
      }),
      map(e => {
        if (e === false) {
          return e;
        } else {
          return true;
        }
      })
    );
  }

  delete(id : number):Observable<boolean>{
    return this.http.delete<any>(`${environment.oeuvreUrl}/${id}`).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server, see log" });
        return of(false);
      }),
      map(e => {
        if (e === false) {
          return e;
        } else {
          return true;
        }
      })
    );
  }

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
          map(es => es.map(e => new Oeuvre(e))),
          catchError(err => {
            console.log("Handling error", err);
            this.ms.push({ content: "Error with server" });
            return this.dataFake
              .asObservable()
              .pipe(map(e => e.filter(pe => pe.themeKey === themeKey)));
          })
        );
    } else {
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
  public add(o :Oeuvre): Observable<Oeuvre> {

    if (environment.online) {
      return this.http.post<Oeuvre>(`${environment.oeuvreUrl}`, o).pipe(map(e=> new Oeuvre(e)),
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
  }
}
