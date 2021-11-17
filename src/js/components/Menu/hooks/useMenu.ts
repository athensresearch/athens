import React from 'react';

const contextMenuAnchor = (e) => ({
  clientHeight: 0,
  clientWidth: 0,
  getBoundingClientRect: () => ({
    width: 0,
    height: 0,
    top: e.clientY,
    right: e.clientX,
    bottom: e.clientY,
    left: e.clientX,
  })
});

type TriggerType = 'contextMenu' | 'click' | 'hover';

export const useMenu = () => {
  const [isOpen, setIsOpen] = React.useState(false);
  const [position, setPosition] = React.useState(null);
  const [anchorEl, setAnchorEl] = React.useState(null);
  const [placement, setPlacement] = React.useState('bottom-start');
  const [triggerType, setTriggerType] = React.useState<TriggerType | null>(null);

  const closeMenu = () => {
    setIsOpen(false);
    setPosition(null);
    setTriggerType(null);
  };

  const triggerProps = (type: TriggerType, placement?) => {
    if (type === 'contextMenu') {
      return ({
        isPressed: triggerType === 'contextMenu',
        onContextMenu: (e) => {
          setAnchorEl(contextMenuAnchor(e));
          e.preventDefault();
          e.stopPropagation();
          setIsOpen(true);
          setPlacement(placement || 'bottom-start');
          setTriggerType('contextMenu');
        }
      });
    } else if (type === 'click') {
      return ({
        isPressed: triggerType === 'click',
        onClick: (e) => {
          setAnchorEl(e.currentTarget);
          setIsOpen(true);
          setPlacement(placement || 'bottom-end');
          setTriggerType('click');
        }
      });
    } else if (type === 'hover') {
      return ({
        isPressed: triggerType === 'hover',
        onMouseEnter: (e) => {
          setAnchorEl(e.currentTarget);
          setIsOpen(true);
          setPlacement(placement || 'bottom-end');
          setTriggerType('hover');
        }
      });
    }
  };

  const menuProps = {
    position,
    anchorEl,
    isOpen,
    placement
  };

  return {
    triggerProps,
    menuProps,
    closeMenu
  };
};
