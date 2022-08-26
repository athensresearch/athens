import React from 'react';
import { ContextMenuContext } from '@/App/ContextMenuContext';
import { Box, Button, Checkbox, MenuGroup, MenuItemOption, MenuOptionGroup } from '@chakra-ui/react';
import { ArrowRightIcon, CheckmarkIcon, ChevronDownIcon, PauseIcon, SquareIcon, XmarkIcon } from '@/Icons/Icons';

interface TaskboxProps {
  status: string;
  onChange: (status: string) => void;
  options: string[];
}

const STATUS_ICON = {
  'To Do': <SquareIcon />,
  'Doing': <ArrowRightIcon />,
  'Blocked': <PauseIcon />,
  'Done': <CheckmarkIcon />,
  'Cancelled': <XmarkIcon />,
}

export const Taskbox = (props: TaskboxProps) => {
  const { status, options, onChange, ...boxProps } = props;
  const { addToContextMenu, getIsMenuOpen } = React.useContext(ContextMenuContext);

  const ref = React.useRef(null);
  const isMenuOpen = getIsMenuOpen(ref);

  console.log(status, STATUS_ICON[status]);

  const Menu = () => {
    return (
      <MenuGroup>
        <MenuOptionGroup
          defaultValue={status}
          title="Status"
          type="radio"
          onChange={(value) => {
            console.log('changing to ', value)
            onChange(value as string)
          }}>
          {options.map((option) => {
            return (<MenuItemOption
              icon={STATUS_ICON[option]}
              key={option}
              value={option}
            >
              {option}
            </MenuItemOption>)
          })}
        </MenuOptionGroup>
      </MenuGroup>
    );
  }

  return <Button
    isActive={isMenuOpen}
    ref={ref}
    isDisabled={!options?.length}
    onClick={(event) => {
      addToContextMenu({ event, ref, component: Menu, anchorEl: ref })
    }}
  >
    {STATUS_ICON[status]}
    <ChevronDownIcon />
  </Button>
}