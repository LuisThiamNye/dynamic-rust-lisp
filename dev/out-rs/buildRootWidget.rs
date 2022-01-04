use druid::widget::prelude::*;
use druid::widget::{Flex, Label, TextBox};
use druid::{Data, Lens, UnitPoint, WidgetExt};

use __globals::{HelloState, TEXT_BOX_WIDTH, VERTICAL_WIDGET_SPACING};

#[no_mangle]
pub extern "C" fn buildRootWidget() -> *const druid::widget::Align<HelloState> {
    /* let */
    {
        // let label = Label::new(|data: &HelloState, _env: &Env| {
        //     if data.name.is_empty() {
        //         "Hello anybody!?".to_string()
        //     } else {
        //         format!("Hello {}!", data.name)
        //     }
        // })
        // .with_text_size(32.0);

        // let textbox = TextBox::new()
        //     .with_placeholder("Who are we greeting?")
        //     .with_text_size(18.0)
        //     .fix_width(TEXT_BOX_WIDTH)
        //     .lens(HelloState::name);

        // let ret = Flex::column()
        //     .with_child(label)
        //     .with_spacer(VERTICAL_WIDGET_SPACING)
        //     .with_child(textbox)
        // .align_vertical(UnitPoint::CENTER);
        // ret
        let ret = druid_widget_Label_new("hi");
        let p = &ret as *const druid::widget::Align<HelloState>;
        std::mem::forget(ret);
        p
    }
}
