
import React from 'react';
import { Box, Popover, PopoverContent, PopoverTrigger, Portal, Button, ButtonGroup, Menu, MenuButton, MenuItem, MenuList } from '@chakra-ui/react';
import { EllipsisHorizontalIcon } from "@/Icons/Icons";
import {
  attrIf,
  showPreview,
  showActions,
  setBlockHovered,
  targetIsAutolink,
  targetIsUrlLink,
  targetIsPageLink,
  targetIsHashtag
} from './utils/utils';

export const InteractionManager = ({
  children,
  isScrolling,
  setIsScrolling,
  shouldSetBlockIsHovered = true,
  shouldShowActions = true,
  shouldShowPreviews,
  onNavigateUid,
  onNavigatePage,
  setPreviewPos,
  setPreview,
  setActionsPos,
  setActions,
}) => {
  const [target, setTarget] = React.useState(null);
  const [lastE, setLastE] = React.useState(null);

  // When hovering a page, link, breadcrumb, etc.
  const handleMouseMove = React.useCallback((e) => {

    if (shouldShowPreviews) {
      showPreview(e, setPreviewPos, setPreview);
    }

    if (shouldSetBlockIsHovered) {
      setBlockHovered(e);
    }

    if (shouldShowActions) {
      showActions(e, lastE, setLastE, setActionsPos, setActions);
    }
  }, [target, shouldShowPreviews, shouldSetBlockIsHovered, shouldShowActions]);


  // When clicking a page, link, breadcrumb, etc.
  const handleClick = (e) => {

    const clickedPage = attrIf(e, targetIsPageLink, 'data-page-title');
    if (clickedPage) {
      onNavigatePage(clickedPage, e);
      return;
    }

    const clickedHashtag = attrIf(e, targetIsHashtag, 'data-page-title');
    if (clickedHashtag) {
      onNavigatePage(clickedHashtag, e);
      return;
    }

    // Anchor links
    const clickedUrlLink = attrIf(e, targetIsUrlLink, 'href');
    if (clickedUrlLink) {
      e.preventDefault();
      e.stopPropagation();
      window.history.pushState({}, '', clickedUrlLink);
      return;
    }

    const clickedAutoLink = attrIf(e, targetIsAutolink, 'href');
    if (clickedAutoLink) {
      e.preventDefault();
      e.stopPropagation();
      window.history.pushState({}, '', clickedAutoLink);
      return;
    }
  }

  return (
    <Box
      display="contents"
      onClick={handleClick}
      onMouseMove={(e) => {
        // update the target in state so we
        // can use it to memoize the mousemove event
        if (e.target !== target) { setTarget(e.target) };
        // then handle the move events
        handleMouseMove(e);
      }}
      onWheel={(e) => {
        if (!isScrolling) {
          setIsScrolling(true)
          setTimeout(() => {
            setIsScrolling(false)
          }, 500)
        }
      }}
    >
      {children}
    </Box>)
}

export const Preview = ({ isOpen, pos, children, isScrolling }) => {
  if (!isOpen) return null;

  return <Popover
    isOpen={true}
    placement="bottom-start"
    autoFocus={false}
  >
    <PopoverTrigger>
      <Box
        position="fixed"
        pointerEvents="none"
        left={pos?.x + 10}
        top={pos?.y + 10}
      />
    </PopoverTrigger>
    <Portal>
      <PopoverContent
        opacity={isScrolling ? 0.5 : 1}
        pointerEvents="none"
        p={4}
        background="background.upper"
        width="20rem"
        overflow="hidden"
      >
        {children}
      </PopoverContent>
    </Portal>
  </Popover>
}

export const Actions = ({ actions, pos, isScrolling }) => {
  if (!actions) return null;
  const extraActions = actions.filter(a => a.isExtra);
  const defaultActions = actions.filter(a => !a.isExtra);

  return (
    <ButtonGroup
      className="block-actions"
      size="xs"
      zIndex="tooltip"
      isAttached={true}
      position="absolute"
      top={pos?.y}
      left={pos?.x}
      transform="translateY(-50%) translateX(-100%)"
      opacity={isScrolling ? 0.5 : 1}
    >
      {defaultActions.map(a => <Button
        key={a.children}
        {...a}
      />)}
      {extraActions.length > 0 && (
        <Menu
          isLazy={true}
        >
          <MenuButton
            as={Button}
            size="xs"
            zIndex="tooltip"
          >
            <EllipsisHorizontalIcon />
          </MenuButton>
          <MenuList>
            {extraActions.map(a => <MenuItem key={a.children} {...a} />)}
          </MenuList>
        </Menu>
      )}
    </ButtonGroup>
  )
}
