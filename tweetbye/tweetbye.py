import twitter
import pickle
from ConfigParser import SafeConfigParser

parser = SafeConfigParser()
parser.read('config.ini')

api = twitter.Api(consumer_key=parser.get('credentials', 'consumer_key'),
                  consumer_secret=parser.get('credentials', 'consumer_secret'),
                  access_token_key=parser.get('credentials', 'access_token_key'),
                  access_token_secret=parser.get('credentials', 'access_token_secret'))

FOLLOWERS_MIN_COUNT = int(parser.get('params', 'followers_min_count'))
FOLLOWERS_MAX_COUNT = int(parser.get('params', 'followers_max_count'))
FOLLOWERS_RATIO = int(parser.get('params', 'followers_ratio'))

FRIENDS_CACHE = parser.get('caches', 'friends')
FOLLOWERS_CACHE = parser.get('caches', 'followers')
VIPS_CACHE = parser.get('caches', 'vips')


def main():
    print('==== Twitter Bye [START] ======')
    print('==== Twitter Bye [WARM-UP] ====')
    friends = get_friends()
    follower_ids = get_follower_ids()
    whitelist = get_vips()
    blacklist = []
    print('==== Twitter Bye [LOOP] ======')
    for f in friends:
        friend = f.AsDict()
        if friend['id'] not in follower_ids:
            friends_count = nvl(friend, 'friends_count', 0)
            followers_count = nvl(friend, 'followers_count', 0)
            if not is_famous(friends_count, followers_count) and not friend['screen_name'] in whitelist:
                choice = raw_input("I am following %s but they don't follow me. Unfollow? [Y/N] " % friend['screen_name'])
                if "N" == choice:
                    whitelist\
                        .append(friend['screen_name'])
                else:
                    blacklist.append(friend['screen_name'])

    pickle.dump(whitelist, open(VIPS_CACHE, 'wb'))
    print('==== Twitter Bye [ACTIONS] ====')
    for follower_name in blacklist:
        print("Go to Twitter and unfollow this account: %s" % follower_name)
    print('==== Twitter Bye [END] ========')


def is_famous(friends_count, followers_count):
    is_lonely_wolf = friends_count == 0
    if is_lonely_wolf:
        return True

    follows_many_people = friends_count > followers_count
    if follows_many_people:
        return False

    has_enough_followers = followers_count > FOLLOWERS_MIN_COUNT
    has_enough_influence = followers_count / (friends_count * 1.0) > FOLLOWERS_RATIO
    has_lots_of_followers = followers_count > FOLLOWERS_MAX_COUNT
    return has_enough_followers and (has_enough_influence or has_lots_of_followers)


def get_friends():
    try:
        friends = pickle.load(open(FRIENDS_CACHE, 'rb'))
        print('[CacheLoading - Friends][SUCCESS] Value retrieved from local cache')
    except IOError:
        print('[CacheLoading - Friends][FAILURE] Could not load from cache. Retrieving from service...')
        friends = api.GetFriends()
        pickle.dump(friends, open(FRIENDS_CACHE, 'wb'))
        print('[CacheLoading - Friends][SUCCESS] Value stored in local cache')
    return friends


def get_follower_ids():
    try:
        follower_ids = pickle.load(open(FOLLOWERS_CACHE, 'rb'))
        print('[CacheLoading - Followers][SUCCESS] Value retrieved from local cache')
    except IOError:
        print('[CacheLoading - Followers][FAILURE] Could not load from cache. Retrieving from service...')
        follower_ids = api.GetFollowerIDs()
        pickle.dump(follower_ids, open(FOLLOWERS_CACHE, 'wb'))
        print('[CacheLoading - Followers][SUCCESS] Value stored in local cache')
    return follower_ids


def get_vips():
    try:
        vips = pickle.load(open(VIPS_CACHE, 'rb'))
        print('[CacheLoading - VIPs][SUCCESS] Value retrieved from local cache')
    except IOError:
        print('[CacheLoading - VIPs][FAILURE] Could not load from cache. Defaulting to empty set...')
        vips = []
    return vips


def nvl(struct, key_name, nvl_value):
    if key_name in struct:
        return struct[key_name]
    return nvl_value


if __name__ == "__main__":
    main()
