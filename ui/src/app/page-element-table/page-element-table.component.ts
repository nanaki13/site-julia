import { Component, OnInit, Input, Output, EventEmitter } from "@angular/core";
import { PageElement } from "../model/PageElement";
import { PageElementDisplay } from "../PageElementDisplay";
import { Updatable } from '../Updatable';
import { SourceService } from '../source.service';

@Component({
  selector: "app-page-element-table",
  templateUrl: "./page-element-table.component.html",
  styleUrls: ["./page-element-table.component.css"]
})
export class PageElementTableComponent implements OnInit {
  @Input()
  element: PageElement;
  @Input()
  pDisplay: PageElementDisplay;
  @Output()
  updateElement= new EventEmitter< PageElement>();
  @Output()
  deleteElement= new EventEmitter< number>();
  constructor(private sourceService : SourceService) {}

  ngOnInit() {
      console.log(this.element);

  }

  src(): string  {return this.pDisplay.src(this.element)}

  update(element: PageElement){
    this.updateElement.emit(element)
  }

  change(img: Updatable, event: Event) {
    img.updated = true;
  }

  delete(id : number){
    this.deleteElement.emit(id);
  }

  click(event){
   this.sourceService.source = this.element;
  }
}
