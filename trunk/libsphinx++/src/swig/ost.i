namespace ost {
 class Event {
   public:
     %rename(waitFor) wait;
     void wait();
     void signal();
 };
}
