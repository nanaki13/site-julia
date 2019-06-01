import { Component, OnInit } from "@angular/core";
import { MessageInternService } from "../message-intern.service";

@Component({
  selector: "app-message",
  templateUrl: "./message.component.html",
  styleUrls: ["./message.component.css"]
})
export class MessageComponent implements OnInit {
  messages: {
    content: string;
    serverity?: string;
  }[];

  constructor(private ms: MessageInternService) {}

  ngOnInit() {
    this.messages = this.ms.messages;
  }
}
