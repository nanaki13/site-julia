import { Injectable } from "@angular/core";
import { environment } from "./../environments/environment";
import { Oeuvre } from "./model/Oeuvre";
import { of, Observable, interval } from "rxjs";
import { map, catchError } from "rxjs/operators";
import { MessageInternService } from "./message-intern.service";
import { HttpClient } from "@angular/common/http";
import { CacheEnv } from './util';
@Injectable({
  providedIn: "root"
})
export class OeuvreService {

  private dataFake = new CacheEnv<Oeuvre>(()=> new Oeuvre());
  constructor(private http: HttpClient, private ms: MessageInternService) {}

  /**
   * Makes a http get request to retrieve the welcome message from the backend service.
   */
  public oauvres(themeKey: number): Observable<Oeuvre[]> {
    if(environment.online){
      return this.http
      .get<Oeuvre[]>(`${environment.oeuvreUrl}`, {
        params: { "theme_key" : `${themeKey}` }
      })
      .pipe(
        catchError(err => {
          console.log("Handling error", err);
          this.ms.push({ content: "Error with server" });
          return this.dataFake.asObservable();
        })
      );
    }else{
      return this.dataFake.asObservable();
    }

  }

  public add(title : String,themeKey: number): Observable<Oeuvre> {
    const o = this.dataFake.newWithId();
    o.themeKey = themeKey;
    o.title = title;
    o.x = 0;
    o.y = 0;
    if(environment.online){
      return this.http
      .post<Oeuvre>(`${environment.oeuvreUrl}`,o).pipe(
        catchError(err => {
          console.log("Handling error", err);
          this.ms.push({ content: "Error with server" });
          this.dataFake.set(o.id,o);
          return of(o);
        })
      );
    }else{
      this.dataFake.set(o.id,o);
      return  of(o);
    }
    return this.http
      .post<Oeuvre>(`${environment.oeuvreUrl}`,o).pipe(
        catchError(err => {
          console.log("Handling error", err);
          this.ms.push({ content: "Error with server" });
          this.dataFake.set(o.id,o);
          return of(o);
        })
      );;
  }
}
