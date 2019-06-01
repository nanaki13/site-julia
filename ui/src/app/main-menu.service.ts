import { Injectable } from "@angular/core";
import { of, Observable, interval } from "rxjs";
import { map, catchError } from "rxjs/operators";
import { HttpClient } from "@angular/common/http";
import { MessageInternService } from "./message-intern.service";
export class MenuItem {

  public id: number;
  constructor(protected  title: String,protected  parentTheme: number = null) {

  }



}
export const emptyMenu = [new MenuItem("No Menu",0)]
@Injectable({
  providedIn: "root"
})
export class MainMenuService {

  private serviceUrl = "/api/summary";
  private dataPostTestUrl = "/api/postTest";
  private menuUrl = "/api/menu";
  private subMenuUrl = "/api/submenu";


  /**
   * Makes a http get request to retrieve the welcome message from the backend service.
   */
  public getWelcomeMessage() {
    return this.http.get(this.serviceUrl).pipe(
      map(response => response)
    );
  }
 /**
   * Makes a http get request to retrieve the welcome message from the backend service.
   */
  public getMenu(): Observable<MenuItem[]>  {
    return this.http.get<MenuItem[]>(this.menuUrl).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server" });
        return of(emptyMenu);
      })
    );
  }
  /**
   * Makes a http post request to send some data to backend & get response.
   */
  public sendData(): Observable<any> {
    return this.http.post(this.dataPostTestUrl, {}).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server" });
        return of([]);
      })
    );;
  }





 // getMenu(): Observable<MenuItem[]> {
  //  return interval(1000).pipe( map(i =>this.fakeData))
  // return of(this.fakeData)
  // }

  getSubMenu(parentTheme: number): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(this.subMenuUrl + "?parentTheme=" + parentTheme );
    }

  constructor(private http: HttpClient,private ms : MessageInternService) {


  }
  addMenu(title: String) {
       const mi = new MenuItem(title);
       return this.http.post(this.menuUrl, mi).pipe(
        catchError(err => {
          console.log("Handling error", err);
          this.ms.push({ content: "Error with server" });
          return of([]);
        })
      );;

  }

  addSubMenu(parentMenuKey: number, subMenuTitle: String) {
    const mi = new MenuItem(subMenuTitle, parentMenuKey);
    return this.http.post(this.subMenuUrl, mi).pipe(
      catchError(err => {
        console.log("Handling error", err);
        this.ms.push({ content: "Error with server" });
        return of([]);
      })
    );;
}
}



