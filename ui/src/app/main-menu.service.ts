import { Injectable } from '@angular/core';
import { of, Observable, interval } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class MainMenuService {

  private serviceUrl = '/api/summary';
  private dataPostTestUrl = '/api/postTest';
  private menuUrl = '/api/menu';

 
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
    return this.http.get<MenuItem[]>(this.menuUrl)
  }
  /**
   * Makes a http post request to send some data to backend & get response.
   */
  public sendData(): Observable<any> {
    return this.http.post(this.dataPostTestUrl, {});
  }
  cnt = 1

   fakeData :  MenuItem[] = [   ]
   subMenu :  Map<String,MenuItem[]> =  new Map()
   
    
 // getMenu(): Observable<MenuItem[]> {
  //  return interval(1000).pipe( map(i =>this.fakeData))
  //return of(this.fakeData)
  //}

  getSubMenu(title : string): Observable<MenuItem[]> {
    //  return interval(1000).pipe( map(i =>this.fakeData))
    return of(this.subMenu.get(title))
    }

  constructor(private http: HttpClient) { 

    
  }
  addMenu(title : String){
       const mi = new MenuItem(title,this.cnt)   
       this.fakeData.push(mi)  
       return this.http.post(this.menuUrl, mi);

  }

  addSubMenu(title : String,subTitle : String){
    const mi = new MenuItem(subTitle,this.cnt)
    var i = this.subMenu.get(title)  
    if(i == undefined){
        i = []
        this.subMenu.set(title,i) 
    }
    i.push(mi)
}
}


export class MenuItem{
  constructor(public title : String, public id : Number){}
}
