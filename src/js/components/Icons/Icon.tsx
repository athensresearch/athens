import React from "react";
import styled from "styled-components";

export const Icon = React.memo(styled.svg.attrs({
  viewBox: "0 0 24 24",
})`
  width: var(--size, 2em);
  height: var(--size, 2em);

  &,
  * {
    vector-effect: non-scaling-stroke;
    stroke-linecap: round;
    stroke-linejoin: round;
  }

  .fill {
    fill: var(--fill, currentColor);
    stroke: none;
  }

  .stroke {
    stroke: var(--stroke, currentColor);
    stroke-width: var(--stroke-width, 1.5);
    fill: none;
  }

  .fill.stroke {
    fill: var(--fill, currentColor);
  }
`);
