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
  oeuvres:  Oeuvre[][] = [];
  dataObservable = of( [ { title : "title" ,
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

  constructor(private route: ActivatedRoute,private mService : MainMenuService) {
    this.oeuvres = [];
    this.dataObservable.subscribe(e => {
        e.forEach( ele => {
          while(this.oeuvres.length <= ele.x){
            this.oeuvres.push(null);
          }
          if(!this.oeuvres[ele.x]){
            this.oeuvres[ele.x] = [];
          }
          while(this.oeuvres.length <= ele.y){
            this.oeuvres[ele.x].push(null);
          }
          this.oeuvres[ele.x][ele.y] = ele;
        } );
    });

  }

  set(set: Oeuvre) {
    while(this.oeuvres.length <= set.x){
      this.oeuvres.push(null);
    }
    if(!this.oeuvres[set.x]){
      this.oeuvres[set.x] = [];
    }
    while(this.oeuvres.length <= set.y){
      this.oeuvres[set.x].push(null);
    }
    this.oeuvres[set.x][set.y] = set;
  }

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

