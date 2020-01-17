import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BtnUpdateComponent } from './btn-update.component';

describe('BtnUpdateComponent', () => {
  let component: BtnUpdateComponent;
  let fixture: ComponentFixture<BtnUpdateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BtnUpdateComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BtnUpdateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
