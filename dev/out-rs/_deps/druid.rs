extern crate _deps as druid;
// pub use _deps::druid::*;

pub extern "C" fn druid_widget_Label_new(text: impl Into<LabelText<HelloState>>) -> Label {
    druid::widget::Label::new(text)
}

pub extern "C" fn druid_widget_Align_centered() {
    druid::widget::Align::centered()
}
