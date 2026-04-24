package test.persistence.mocks;

import persistence.interfaces.UnitOfWork;

public class MockUnitOfWork implements UnitOfWork
{

  public boolean commit = false;
  public boolean rollback = false;

  @Override public void beginTransaction()
  {}

  @Override public void commit() {commit = true;}

  @Override public void rollback()
  {
    rollback = true;
  }
}
