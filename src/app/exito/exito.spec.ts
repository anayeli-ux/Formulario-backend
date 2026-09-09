import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Exito } from './exito';

describe('Exito', () => {
  let component: Exito;
  let fixture: ComponentFixture<Exito>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Exito],
    }).compileComponents();

    fixture = TestBed.createComponent(Exito);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
