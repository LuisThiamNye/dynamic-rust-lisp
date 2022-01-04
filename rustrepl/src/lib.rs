use libloading::{Library, Symbol};
use std::any::Any;

// pub struct __GenericObj {
//   filepath: String,
//   typename: String,
//   obj: &dyn Any;
// }

// impl __GenericObj {
//   pub fn invoke(&self, method: &str) -> __GenericObj {
//     let lib = Library::new(self.filepath).unwrap();
//     let fnname = self.typename + "__" + method + "__dyn" + "\0";
//     let rmain: Symbol<unsafe extern "C" fn(&dyn Any, Vec<&dyn Any>) -> Any> = lib.get(fnname.to_bytes()).unwrap();
//   }
// }
