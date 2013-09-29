/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

/**
 *
 * @author bansal
 */
public class Test extends Message{
  String data;
  Test(){
      super(MsgContent.ABORT, MsgType.Decision, 0);
      super.data="100";
  }
  public void test(){
      this.data="10";
      super.data="";
  }

    @Override
    public String toString() {
        return "Test{" + "data=" + data + '}'+super.toString();
    }
    
}
