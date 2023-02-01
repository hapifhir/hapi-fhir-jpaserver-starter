import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OperationOutcomeComponent } from './operation-outcome.component';

describe('OperationOutcomeComponent', () => {
  let component: OperationOutcomeComponent;
  let fixture: ComponentFixture<OperationOutcomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OperationOutcomeComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OperationOutcomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
