"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Small = exports.Large = exports.Secondary = exports.Primary = void 0;
const jsx_runtime_1 = require("react/jsx-runtime");
require("./button.css");
const Button_1 = require("./Button");
exports.default = {
    title: 'Example/Button',
    component: Button_1.Button,
    argTypes: {
        backgroundColor: { control: 'color' },
    },
};
const Template = (args) => jsx_runtime_1.jsx(Button_1.Button, Object.assign({}, args), void 0);
exports.Primary = Template.bind({});
exports.Primary.args = {
    primary: true,
    label: 'Button',
};
exports.Secondary = Template.bind({});
exports.Secondary.args = {
    label: 'Button',
};
exports.Large = Template.bind({});
exports.Large.args = {
    size: 'large',
    label: 'Button',
};
exports.Small = Template.bind({});
exports.Small.args = {
    size: 'small',
    label: 'Button',
};
