import { Injectable } from "@angular/core";
import { of, Observable, interval } from "rxjs";
import { map, catchError } from "rxjs/operators";
import { HttpClient } from "@angular/common/http";
import { MessageInternService } from "./message-intern.service";
import { environment } from "./../environments/environment";
import { identifierModuleUrl } from "@angular/compiler";
import { AutoMap, AutoId } from "./util";
import { MenuItem } from './MenuItem';
@Injectable({
  providedIn: "root"
})
export class MainMenuService {
  private serviceUrl = environment.serviceUrl;
  private menuUrl = "/api/menu";
  private subMenuUrl = "/api/submenu";

  private rootMenuAutoId = new AutoId();
  private fakeData: MenuItem[] = [];
  private fakeSubMenu: AutoMap<Number, MenuItem[]> = new AutoMap(() => []);


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

  // getMenu(): Observable<MenuItem[]> {
  //  return interval(1000).pipe( map(i =>this.fakeData))
  // return of(this.fakeData)
  // }

  getSubMenu(parentTheme: number): Observable<MenuItem[]> {
    if (environment.online) {
      return this.http
        .get<MenuItem[]>(this.subMenuUrl + "?theme_key=" + parentTheme)
        .pipe(
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
    debugger;
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
