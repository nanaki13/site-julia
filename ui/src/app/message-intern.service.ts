import { Injectable } from "@angular/core";

@Injectable({
  providedIn: "root"
})

export class MessageInternService {
  static inter:  number;
  static checkInter = false;
  private _messages: { content: string; serverity?: string }[] = [
    { content: "no message" }
  ];

  static removeFirst(mes: Array<any>) {
    if (mes.length > 0) {
      mes.shift()


    }else{
      MessageInternService.checkInter = false;
      window.clearInterval(MessageInternService.inter)
    }


  }
  push(message: { content: string }) {
    this._messages.push(message)
    if( !MessageInternService.checkInter){
      MessageInternService.checkInter = true;
      MessageInternService.inter = window.setInterval(MessageInternService.removeFirst,1200,this._messages)
    }


  }



  get messages() {return this._messages; }
  constructor() {}
}
