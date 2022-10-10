/**
 * File: Column.java
 * Package: com.cerner.management.wtslocation
 * Project: management-wtslocation
 */
package com.cerner.management.wtslocation;

public class Column
{
   private String columnName;

   public Column(String columnName) {
      this.columnName = columnName;
   }
   public String getColumnName()
   {
      return columnName;
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
      return result;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Column other = (Column) obj;
      if ((columnName == null) ||((other!=null) && (other.columnName == null)))
         return false;
      else if (!columnName.equalsIgnoreCase(other.columnName))
         return false;
      return true;
   }
   
}
