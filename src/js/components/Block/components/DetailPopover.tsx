import React from 'react';
import styled from 'styled-components';
import { TooltipTriggerState } from '@react-stately/tooltip';
import { useTooltip } from '@react-aria/tooltip';

import { Overlay } from '@/Overlay';

const Details = styled(Overlay)`
  height: max-content;
  width: max-content;
  display: flex;
  flex-direction: column;
  list-style: none;
  margin: 0;
  padding: 0.25rem 0.5rem;
  font-size: var(--font-size--text-sm);
  gap: -0.125rem;
  position: relative;
  top: 0.25rem;
  left: 2.5rem;
  z-index: 9999;
  line-height: 1.3;
`;

const Item = styled.li`
  margin: 0;
  padding: 0;
  display: flex;
  justify-content: space-between;

  span {
    color: var(--body-text-color---opacity-med);
    flex: 1 1 50%;
  }
  span + span {
    margin-left: 1ch;
    color: var(--body-text-color);
  }
`;

const showValue = (value) => {
  if (typeof value === 'object') return (value = JSON.stringify(value));
  else if (typeof value === 'boolean') return (value = value ? 'true' : 'false');
  else return value;
}

interface DetailPopoverProps extends React.HTMLAttributes<HTMLDivElement> {
  state: TooltipTriggerState;
  block: any;
}

export const DetailPopover = React.forwardRef((props: DetailPopoverProps, ref) => {
  const { block, state, } = props;
  let { tooltipProps } = useTooltip(props, state);

  const properties = {
    "uid": block.uid,
    "db/id": block.id,
    "order": block.order,
    "open": block.open,
    "refs": block._refs?.length || 0,
  }

  return (
    <Details
      as="ul"
      ref={ref}
      {...tooltipProps}
      onMouseDown={(e) => { e.stopPropagation(); }}
      onClick={(e) => { e.stopPropagation(); }}
    >
      {Object.entries(properties).map(([key, value]) => <Item key={key}>
        <span>{key}</span> <span>{showValue(value)}</span>
      </Item>)}
    </Details>
  );
});
