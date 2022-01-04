use libloading::{Library, Symbol};
use std::thread::Thread;

fn main() {
  unsafe {
    loop {
      let lib = Library::new("../dev/target/dynamic/debug/deps/libhello__main.dylib").unwrap();
      let rmain: Symbol<unsafe extern "C" fn()> = lib.get(b"main\0").unwrap();
      rmain();
      std::thread::sleep(std::time::Duration::from_secs(1));
    }
  }
}
