use druid::widget::prelude::*;
use druid::widget::Flex;
use druid::{AppLauncher, WindowDesc};
use druid::{Data, Lens, UnitPoint};
use libloading::{Library, Symbol};

use __globals::HelloState;

fn build_root_widget() -> druid::widget::Align<HelloState> {
    unsafe {
        let lib = Library::new("../dev/target/dynamic/debug/deps/libhello__buildRootWidget.dylib")
            .unwrap();
        println!("loading");
        let f: Symbol<unsafe extern "C" fn() -> *const druid::widget::Align<HelloState>> =
            lib.get(b"buildRootWidget\0").unwrap();
        println!("loaded");
        let x = f();
        println!("runned");
        let x = std::ptr::read(x);
        println!("runned2");
        x
    }
}

#[no_mangle]
pub extern "C" fn main() {
    /* let */
    {
        let root = build_root_widget();
        // let main_window = WindowDesc::new(druid::widget::Label::new("hi"))
        //     .title("Hello World!")
        //     .window_size((400.0, 400.0));
        let main_window = WindowDesc::new(root)
            .title("Hello World!")
            .window_size((400.0, 400.0));
        println!("1");
        // println!("{:?}", root.name);

        let initial_state: HelloState = HelloState {
            name: "World".into(),
        };
        println!("2");

        AppLauncher::with_window(main_window)
            .launch(initial_state)
            .expect("Failed to launch application");
    }
}
