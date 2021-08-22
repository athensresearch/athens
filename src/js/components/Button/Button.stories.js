"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Icon = exports.Pressed = exports.isPrimary = exports.Primary = void 0;
const jsx_runtime_1 = require("react/jsx-runtime");
const Button_1 = require("./Button");
exports.default = {
    title: 'Example/Button',
    component: Button_1.Button,
    argTypes: {},
};
const Template = (args) => jsx_runtime_1.jsx(Button_1.Button, Object.assign({}, args), void 0);
exports.Primary = Template.bind({});
exports.Primary.args = {
    children: 'Button',
};
exports.isPrimary = Template.bind({});
exports.isPrimary.args = {
    isPrimary: true,
    children: 'Button',
};
exports.Pressed = Template.bind({});
exports.Pressed.args = {
    "aria-pressed": true,
    children: 'Button',
};
exports.Icon = Template.bind({});
exports.Icon.args = {
    children: jsx_runtime_1.jsxs("svg", Object.assign({ width: "24", height: "24", "stroke-width": "1.5", viewBox: "0 0 24 24", fill: "none", xmlns: "http://www.w3.org/2000/svg" }, { children: [jsx_runtime_1.jsx("path", { d: "M9 12H12M15 12H12M12 12V9M12 12V15", stroke: "currentColor", "stroke-linecap": "round", "stroke-linejoin": "round" }, void 0), jsx_runtime_1.jsx("path", { d: "M21 3.6V20.4C21 20.7314 20.7314 21 20.4 21H3.6C3.26863 21 3 20.7314 3 20.4V3.6C3 3.26863 3.26863 3 3.6 3H20.4C20.7314 3 21 3.26863 21 3.6Z", stroke: "currentColor", "stroke-linecap": "round", "stroke-linejoin": "round" }, void 0)] }), void 0),
};
