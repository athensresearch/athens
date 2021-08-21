"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Button = void 0;
const tslib_1 = require("tslib");
const jsx_runtime_1 = require("react/jsx-runtime");
const react_1 = require("@linaria/react");
const StyledButton = react_1.styled.button `
  font-family: 'IBM Plex Sans';
  font-weight: 700;
  border: 0;
  border-radius: 3em;
  cursor: pointer;
  display: inline-block;
  line-height: 1;

  &.storybook-button--primary {
    color: white;
  }

  &.storybook-button--secondary {
    color: #333;
    box-shadow: rgba(0, 0, 0, 0.15) 0px 0px 0px 1px inset;
  }

  &.storybook-button--small {
    font-size: 12px;
    padding: 10px 16px;
  }

  &.storybook-button--medium {
    font-size: 14px;
    padding: 11px 20px;
  }

  &.storybook-button--large {
    font-size: 16px;
    padding: 12px 24px;
  }
`;
/**
 * Primary UI component for user interaction
 */
const Button = (_a) => {
    var { primary = false, backgroundColor = "", size = 'medium', label } = _a, props = tslib_1.__rest(_a, ["primary", "backgroundColor", "size", "label"]);
    const mode = primary ? 'storybook-button--primary' : 'storybook-button--secondary';
    return (jsx_runtime_1.jsx(StyledButton, Object.assign({ type: "button", className: ['storybook-button', `storybook-button--${size}`, mode].join(' '), style: { backgroundColor } }, props, { children: label }), void 0));
};
exports.Button = Button;
