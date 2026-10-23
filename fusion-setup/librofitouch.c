#define _GNU_SOURCE
#include <dlfcn.h>
#include <stdint.h>
#include <stdlib.h>
#include <unistd.h>

typedef struct xcb_generic_event_t {
    uint8_t   response_type;
    uint8_t   pad0;
    uint16_t  sequence;
    uint32_t  pad[7];
    uint32_t  full_sequence;
} xcb_generic_event_t;

typedef struct {
    uint8_t   response_type;
    uint8_t   detail;
    uint16_t  sequence;
    uint32_t  time;
    uint32_t  root;
    uint32_t  event;
    uint32_t  child;
    int16_t   root_x;
    int16_t   root_y;
    int16_t   event_x;
    int16_t   event_y;
    uint16_t  state;
    uint8_t   same_screen;
    uint8_t   pad0;
} xcb_button_press_event_t;

typedef xcb_generic_event_t* (*poll_event_fn)(void*);

xcb_generic_event_t *xcb_poll_for_event(void *c) {
    static poll_event_fn real_poll = NULL;
    if (!real_poll) {
        real_poll = (poll_event_fn)dlsym(RTLD_NEXT, "xcb_poll_for_event");
    }
    xcb_generic_event_t *ev = real_poll ? real_poll(c) : NULL;
    if (ev) {
        uint8_t type = ev->response_type & 0x7f;
        if (type == 4 /* XCB_BUTTON_PRESS */) {
            xcb_button_press_event_t *bev = (xcb_button_press_event_t*)ev;
            // Detail 1 is Left Click (Touch tap)
            // Top area (y < 160) is the Search/Input bar in Rofi
            if (bev->detail == 1 && bev->event_y < 160) {
                pid_t pid = fork();
                if (pid == 0) {
                    close(0); close(1); close(2);
                    execlp("termux-ipc", "termux-ipc", "show", (char*)NULL);
                    execl("/usr/local/bin/termux-ipc", "termux-ipc", "show", (char*)NULL);
                    execl("/usr/local/termux-shims/termux-ipc", "termux-ipc", "show", (char*)NULL);
                    execl("/data/data/com.termux/files/usr/bin/termux-ipc", "termux-ipc", "show", (char*)NULL);
                    _exit(0);
                }
            }
        }
    }
    return ev;
}
