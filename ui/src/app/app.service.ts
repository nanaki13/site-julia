import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { map, catchError } from 'rxjs/operators';
import { Observable, of } from 'rxjs/index';
import { MessageInternService } from './message-intern.service';
import { environment } from "./../environments/environment";

/**
 * Class representing application service.
 *
 * @class AppService.
 */
@Injectable()
export class AppService {
  private serviceUrl = '/api/summary';
  private dataPostTestUrl = '/api/postTest';
  private dataPostImage = '/api/image';

  constructor(private http: HttpClient,private ms : MessageInternService) {
  }

  /**
   * Makes a http get request to retrieve the welcome message from the backend service.
   */
  public getWelcomeMessage() {
    return this.http.get(this.serviceUrl).pipe(
      map(response => response)
    );
  }

  /**
   * Makes a http post request to send some data to backend & get response.
   */
  public sendData(data): Observable<any> {
    return this.http.post(this.dataPostTestUrl, data);
  }



     /**
     * Makes a http post request to send some data to backend & get response.
     */
    public sendImageTo(data): Observable<any> {
      return this.http.post(environment.imageUrl, data).pipe(
        catchError(err => {

          console.log("Handling error", err);
          this.ms.push({ content: "Error with server, dev mode" });
          return of({link : "FAKE LINK"});
        })
      );
    }
}
