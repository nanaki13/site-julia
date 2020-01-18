import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplayModeCommandComponent } from './display-mode-command.component';

describe('DisplayModeCommandComponent', () => {
  let component: DisplayModeCommandComponent;
  let fixture: ComponentFixture<DisplayModeCommandComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DisplayModeCommandComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DisplayModeCommandComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
