"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Button = void 0;
const tslib_1 = require("tslib");
const jsx_runtime_1 = require("react/jsx-runtime");
const styled_components_1 = tslib_1.__importDefault(require("styled-components"));
const StyledButton = styled_components_1.default.button `
  cursor: pointer;
  padding: 0.375rem 0.625rem;
  margin: 0;
  font-family: inherit;
  font-size: inherit;
  border-radius: 0.25rem;
  font-weight: 500;
  border: none;
  display: inline-flex;
  align-items: center;
  color: var(--body-text-color);
  background-color: transparent;
  transition-property: filter, background, color, opacity;
  transition-duration: 0.075s;
  transition-timing-function: ease;

  &:hover {
    background: var(--body-text-color---opacity-lower);
  }

  &:active,
  &:hover:active,
  &[aria-pressed="true"] {
    color: var(--body-text-color);
    background: var(--body-text-color---opacity-lower);
  }

  &:active,
  &:hover:active,
  &:active[aria-pressed="true"] {
    background: var(--body-text-color---opacity-low);
  }

  &:disabled,
  &:disabled:active {
    color: var(--body-text-color---opacity-low);
    background: var(--body-text-color---opacity-lower);
    cursor: default;
  }

  span {
    flex: 1 0 auto;
    text-align: left;
  }

  kbd {
    margin-inline-start: 1rem;
    font-size: 85%;
  }

  > svg {
    margin: -0.0835em -0.325rem;

    &:not(:first-child) {
      margin-left: 0.251em;
    }
    &:not(:last-child) {
      margin-right: 0.251em;
    }
  }

  &.is-primary {
    color: var(--link-color);
    background: var(--link-color---opacity-lower);

    &:hover {
      background: var(--link-color---opacity-low);
    }

    &:active,
    &:hover:active,
    &[aria-pressed="true"] {
      color: white;
      background: var(--link-color);
    }

    &:disabled,
    &:disabled:active {
      color: var(--body-text-color---opacity-low);
      background: var(--body-text-color---opacity-lower);
      cursor: default;
    }
  }
`;
/**
 * Primary UI component for user interaction
 */
const Button = (_a) => {
    var { children, isPrimary, isPressed } = _a, props = tslib_1.__rest(_a, ["children", "isPrimary", "isPressed"]);
    return (jsx_runtime_1.jsx(StyledButton, Object.assign({ type: "button", "aria-pressed": isPressed ? isPressed : undefined, className: [
            'button',
            isPrimary && 'is-primary'
        ].join(' ') }, props, { children: children }), void 0));
};
exports.Button = Button;
