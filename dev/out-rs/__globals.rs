use druid::{AppLauncher, Data, Lens, UnitPoint};

pub const VERTICAL_WIDGET_SPACING: f64 = 20.0;
pub const TEXT_BOX_WIDTH: f64 = 200.0;

#[derive(Clone, Data, Lens)]
#[repr(C)]
pub struct HelloState {
    pub name: String,
}

// use std::any::Any;
// druid_WindowDesc_new(root: &'static FnOnce() -> &dyn Any) -> {
//   WindowDesc::new(root)
// }

// #[no_mangle]
// pub extern "C" fn HelloState__druid_AppLauncher_launch(
//     al: AppLauncher<HelloState>,
//     x: HelloState,
// ) -> Result<(), druid::PlatformError> {
//     al.launch(x)
// }
