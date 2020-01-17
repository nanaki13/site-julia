import { Injectable, EventEmitter } from "@angular/core";
import { of, Observable, interval } from "rxjs";
import { map, catchError } from "rxjs/operators";
import { HttpClient } from "@angular/common/http";
import { MessageInternService } from "./message-intern.service";
import { environment } from "./../environments/environment";
import { identifierModuleUrl } from "@angular/compiler";
import { AutoMap } from "./util";
import { AutoId } from "./AutoId";
import { MenuItem } from './MenuItem';
import { Service } from './image-view/image-view.component';
@Injectable({
  providedIn: "root"
})
export class MainMenuService implements Service {

  private serviceUrl = environment.serviceUrl;
  private menuUrl = environment.menuUrl;
  private subMenuUrl =environment.subMenuUrl;
  private rootMenuAutoId = new AutoId();
  private fakeData: MenuItem[] = [];
  private fakeSubMenu: AutoMap<Number, MenuItem[]> = new AutoMap(() => []);

  private _currentMenuItem = new MenuItem({title : "current"});
  get currentMenuItem(): MenuItem {
    return this._currentMenuItem;
  }

  constructor(private http: HttpClient, private ms: MessageInternService) {}
  /**
   * Makes a http get request to retrieve the welcome message from the backend service.
   */
  public getWelcomeMessage() {
    return this.http.get(this.serviceUrl).pipe(map(response => response));
  }
  /**
   * Makes a http get request to retrieve the welcome message from the backend service.
   */
  public getMenu(): Observable<MenuItem[]> {
    if (environment.online) {
      return this.http.get<MenuItem[]>(this.menuUrl).pipe(
        catchError(err => {
          console.log("Handling error", err);
          this.ms.push({ content: "Error with server" });
          return of(this.fakeData);
        })
      );
    } else {
      return of(this.fakeData);
    }
  }
  delete(id : number):Observable<boolean>{
    return this.http.delete<any>(`${environment.menuUrl}/${id}`).pipe(
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
  update(t: MenuItem): Observable<boolean> {
    return this.http.patch<any>(environment.menuUrl, t).pipe(
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
  // getMenu(): Observable<MenuItem[]> {
  //  return interval(1000).pipe( map(i =>this.fakeData))
  // return of(this.fakeData)
  // }

  getSubMenu(parentTheme: number): Observable<MenuItem[]> {
    if (environment.online) {
      return this.http
        .get<MenuItem[]>(this.subMenuUrl + "?theme_key=" + parentTheme)
        .pipe(
        map(e => {

              return e.map( mi =>
                {
                  const n =new MenuItem();
                  Object.assign(n , mi);

                  return n;
                });
            }),
          catchError(err => {
            console.log("Handling error", err);
            this.ms.push({ content: "Error with server" });
            return of(this.fakeSubMenu.get(parentTheme));
          })
        );
    } else {

      return of(this.fakeSubMenu.get(parentTheme));
    }
  }

  addMenu(title: string) {
    const mi = new MenuItem({ title: title, id: this.rootMenuAutoId.getId() });
    if (environment.online) {
      return this.http.post<MenuItem>(this.menuUrl, mi).pipe(
        catchError(err => {
          console.log("Handling error", err);
          this.fakeData.push(mi);
          this.ms.push({ content: "Error with server" });
          return of(mi);
        })
      );
    } else {
      this.fakeData.push(mi);
      return of(mi);
    }
  }

  saveLocalContext(){
  //  localStorage.setItem("ov",JSON.stringify(this.dataFake.getData) );
  }

  restoreLocalContext(){
//    const back = JSON.parse(localStorage.getItem("ov")) as Oeuvre[];

  }
  addSubMenu( m:MenuItem):Observable<MenuItem> {
    m.id = this.rootMenuAutoId.getId()
    if (environment.online) {
      return this.http.post<MenuItem>(this.subMenuUrl, m).pipe(
        catchError(err => {
          console.log("Handling error", err);
          this.ms.push({ content: "Error with server" });
          this.fakeSubMenu.get(m.themeKey).push(m);
          return of(m);
        })
      );
    } else {
      this.fakeSubMenu.get(m.themeKey).push(m);
      return of(m);
    }
  }
}
