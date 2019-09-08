import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { MainMenuService } from '../main-menu.service';
import { of } from 'rxjs';

@Component({
  selector: 'app-oeuvre',
  templateUrl: './oeuvre.component.html',
  styleUrls: ['./oeuvre.component.css']
})
export class OeuvreComponent implements OnInit {

  newTitle : String;
  title: string;
  id: number;

  data = of( [ { title : "title" ,
    date : "date",
    dimension : "dimension",
    description : "description",x : 1 , y : 0} as Oeuvre,{ title : "title" ,
    date : "date",
    dimension : "dimension",
    description : "description",x : 1 , y : 1} as Oeuvre,{ title : "title" ,
    date : "date",
    dimension : "dimension",
    description : "description",x : 0 , y : 1} as Oeuvre,{ title : "title" ,
    date : "date",
    dimension : "dimension",
    description : "description",x : 0 , y : 0} as Oeuvre ])

  constructor(private route: ActivatedRoute,private mService : MainMenuService) { }

  ngOnInit() {

      this.route.paramMap.subscribe( e => this.loadItem(e))

  }
  loadItem(e: ParamMap): void {
    this.title = e.get('title')
    this.id = +e.get('id')

  }
}
export class Oeuvre{
  title : String;
  date : String;
  dimension : String;
  description : String;
  x : number;
  y :  number;

}

